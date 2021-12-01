"""Warteschlangensimulator Python socket connector"""

from typing import Union

import os
import subprocess
import socket


class QS_socket_connect:
    """Allows to communicate with Warteschlangensimulator via a socket connection"""

    def __init__(self, host: str, port: int) -> None:
        """Allows to communicate with Warteschlangensimulator via a socket connection

        Args:
            host (str): Host address to connect to
            port (int): Port to use
        """
        self.__host: str = host
        self.__port: int = port

    def __send_bytes(self, socket, bytes: Union[bytes, bytearray]):
        socket.sendall(len(bytes).to_bytes(4, "big"))
        socket.sendall(bytes)

    def __send_string(self, socket, text: str):
        self.__send_bytes(socket, bytearray(text, 'utf-8'))

    def __send_task(self, task_data: Union[str, bytes, bytearray]):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as connection:
            try:
                connection.connect((self.__host, self.__port))

                self.__send_string(connection, "Task")

                format_ok: bool = False

                if (isinstance(task_data, (bytes, bytearray))):
                    self.__send_bytes(connection, task_data)
                    format_ok = True

                if (isinstance(task_data, (str))):
                    self.__send_string(connection, task_data)
                    format_ok = True

                if not format_ok:
                    raise RuntimeError("Invalid task format")

                status: str = self.__receive_string(connection)
                info: str = self.__receive_string(connection)

                if status == "ERROR": raise RuntimeError(info)
                if status == "TASKID": return info
                raise RuntimeError("Unknown response format")
            finally:
                connection.close()

    def __direct_process_task(self, task_data: Union[str, bytes, bytearray]):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as connection:
            try:
                connection.connect((self.__host, self.__port))

                self.__send_string(connection, "Task_Now")

                format_ok: bool = False

                if (isinstance(task_data, (bytes, bytearray))):
                    self.__send_bytes(connection, task_data)
                    format_ok = True

                if (isinstance(task_data, (str))):
                    self.__send_string(connection, task_data)
                    format_ok = True

                if not format_ok:
                    raise RuntimeError("Invalid task format")

                status: str = self.__receive_string(connection)

                if status == "ERROR":
                    info: str = self.__receive_string(connection)
                    raise RuntimeError(info)
                if status == "RESULT":
                    return self.__receive_bytes(connection)
                raise RuntimeError("Unknown response format")
            finally:
                connection.close()

    def __receive_bytes(self, socket):
        size: int = int.from_bytes(socket.recv(4), "big")
        chunks = []
        read = 0
        while (read < size):
            chunk = socket.recv(min(size - read, 2048))
            if (len(chunk) == 0):
                raise RuntimeError("Connection broken")
            chunks.append(chunk)
            read += len(chunk)
        return b''.join(chunks)

    def __receive_string(self, socket) -> str:
        return str(self.__receive_bytes(socket), 'utf-8')

    def __receive_task_results(self, task_id: str) -> bytes:
        while True:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as connection:
                try:
                    connection.connect((self.__host, self.__port))
                    self.__send_string(connection, "Result")
                    self.__send_string(connection, str(task_id))
                    status: str = self.__receive_string(connection)
                    if status == "ERROR":
                        self.__receive_string(connection)
                        continue
                    if status == "RESULT":
                        return self.__receive_bytes(connection)
                    raise RuntimeError("Unknown response format")
                finally:
                    connection.close()

    def get_status(self) -> str:
        """Returns the current status of the simulation server.

        Raises:
            RuntimeError: On invalid server answer

        Returns:
            str: Status of the simulation server
        """
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as connection:
            try:
                self.__send_string(connection, "Status")
                status: str = self.__receive_string(connection)
                info: str = self.__receive_string(connection)
                if status == "MESSAGE":
                    return info
                if status == "ERROR":
                    raise RuntimeError(info)
                raise RuntimeError("Unknown response format")
            finally:
                connection.close()

    def run_task(self, task_data: Union[str, bytes, bytearray], direct_process: bool = True) -> bytes:
        """Submits a simulation task to Warteschlangensimulator, runs the simulation and returns the results

        Args:
            task_data (Union[str, bytes, bytearray]): Simulation model or parameter series.
            direct_process (bool, optional): Force task to be executed now. Defaults to True.

        Raises:
            RuntimeError: Returns an error if Warteschlangensimulator cannot be started or no connection can be established

        Returns:
            bytes: Simulation results (xml statistics or zip statistics etc.)
        """
        if direct_process:
            return self.__direct_process_task(task_data)
        else:
            task_id: str = self.__send_task(task_data)
            return self.__receive_task_results(task_id)


class QS_full_connect:
    """Runs a Warteschlangensimulator instance in background"""

    def __init__(self, java_path: str, simulator_path: str, port: int = 1000, timeout: float = -1.0):
        """Runs a Warteschlangensimulator instance in background

        Args:
            java_path (str): Path to Java environment (not "bin" folder, just Java main folder)
            simulator_path (str): Path to Simulator.jar (only path without file name)
            port (int, optional): Port to use. Defaults to 1000.
            timeout (float, option): Number of seconds before simulation will be canceled. Negative values means there is no timeout. Defaults to -1.0.
        """
        if not java_path.endswith(os.path.sep): java_path += os.path.sep
        self.__java: str = java_path + "bin" + os.path.sep + "java"

        if not simulator_path.endswith(os.path.sep): simulator_path += os.path.sep
        self.__simulator: str = simulator_path + "Simulator.jar"

        self.__port: int = port
        self.__timeout: float = timeout

        self.__process = None

    def __enter__(self):
        if self.__process is not None: raise RuntimeError("Simulator already running")

        # Start simulator
        args = [self.__java, "-jar", self.__simulator, "serverSocket", str(self.__port)]
        if self.__timeout>0: args.append(str(self.__timeout))
        self.__process = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        if self.__process.stdout is None: raise RuntimeError("Cannot start simulator")

        # Wait for server is ready
        self.__process.stdout.readline()
        self.__process.stdout.readline()

        return QS_socket_connect("127.0.0.1", self.__port)

    def __exit__(self, exc_type, exc_val, exc_tb):
        if self.__process is None: return False

        # Stop simulator
        if self.__process.stdin is not None:
            self.__process.stdin.write(bytearray("\n", "utf-8"))
            self.__process.stdin.flush()

        self.__process = None
