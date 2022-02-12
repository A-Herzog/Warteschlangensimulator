"""Warteschlangensimulator Python socket connector"""

from typing import Union

import os
import subprocess
import socket


class QS_socket_connect:
    """Allows to communicate with Warteschlangensimulator via a socket connection"""

    def __init__(self, host: str = "localhost", port: int = 10000) -> None:
        """Allows to communicate with Warteschlangensimulator via a socket connection

        Args:
            host (str, optional): Host address to connect to. Default to "localhost"
            port (int, optional): Port to use. Defaults to 10000.
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

    def __init__(self, java_path: Union[str, None] = None, simulator_path: Union[str, None] = None, port: int = 10000, timeout: float = -1.0):
        """Runs a Warteschlangensimulator instance in background

        Args:
            java_path (str, optional): Path to Java environment (Java main folder, "bin" folder, or Java executable). Defaults to None.
            simulator_path (str,option ): Path to Simulator.jar (path without file name or path including jar file). Defaults to None.
            port (int, optional): Port to use. Defaults to 10000.
            timeout (float, option): Number of seconds before simulation will be canceled. Negative values means there is no timeout. Defaults to -1.0.
        """
        java_path = get_java_path(java_path)
        if java_path is None: raise RuntimeError("Java not found")
        self.__java: str = java_path

        simulator_path = get_simulator_path(simulator_path)
        if simulator_path is None: raise RuntimeError("Simulator.jar not found")
        self.__simulator: str = simulator_path

        self.__port: int = port
        self.__timeout: float = timeout

        self.__process = None

    def __enter__(self):
        if self.__process is not None: raise RuntimeError("Simulator already running")

        # Start simulator
        args = [self.__java, "-jar", self.__simulator, "serverSocket", str(self.__port)]
        if self.__timeout > 0: args.append(str(self.__timeout))
        self.__process = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        if self.__process.stdout is None: raise RuntimeError("Cannot start simulator")

        # Wait for server is ready
        self.__process.stdout.readline()
        self.__process.stdout.readline()

        return QS_socket_connect("localhost", self.__port)

    def __exit__(self, exc_type, exc_val, exc_tb):
        if self.__process is None: return False

        # Stop simulator
        if self.__process.stdin is not None:
            try:
                self.__process.stdin.write(bytearray("\n", "utf-8"))
                self.__process.stdin.flush()
            except BrokenPipeError:
                pass

        self.__process = None


def get_java_path(user_suggested_path: Union[str, None] = None) -> Union[str, None]:
    """Trys to find a Java environment

    Args:
        user_suggested_path (Union[str,None], optional): Path to check first. Default to None.

    Returns:
        Union[str, None]: Path to Java environment or None if not found
    """
    path_list = []

    # User suggested path
    if user_suggested_path is not None: path_list.append(user_suggested_path)

    # Linux default Java path
    path_list.append("/usr/bin")

    # Search in current directory and user home directory
    path_list.append(os.path.expanduser('~') + os.path.sep + "Java")
    path_list.append(os.path.expanduser('~') + os.path.sep + "jdk")
    path_list.append(os.path.expanduser('~') + os.path.sep + "jre")
    path_list.append(os.path.abspath('.') + os.path.sep + "Java")
    path_list.append(os.path.abspath('.') + os.path.sep + "jdk")
    path_list.append(os.path.abspath('.') + os.path.sep + "jre")

    # Java home environment variable
    if 'JAVA_HOME' in os.environ: path_list.append(os.environ['JAVA_HOME'])

    # Common java distribution paths
    path_list.append("C:\\Program Files\\Eclipse Adoptium")
    path_list.append("C:\\Program Files\\Eclipse Foundation")
    path_list.append("C:\\Program Files\\AdoptOpenJDK")
    path_list.append("C:\\Program Files\\Java")
    path_list.append("C:\\Program Files\\Amazon Corretto")
    path_list.append("C:\\Program Files\\Zulu")
    path_list.append("C:\\Program Files\\Microsoft")

    # Path environment variable
    if 'PATH' in os.environ:
        for path in os.environ['PATH'].split(os.pathsep): path_list.append(path)

    for path in path_list:
        result = __test_java_path(path)
        if result is not None: return result

    return None


def __test_java_path(path: str, allow_sub_folders: int = 3) -> Union[str, None]:
    if not path.endswith(os.sep): path += os.sep
    if not os.path.isdir(path): return None

    if os.path.isfile(path + "java"): return path + "java"
    if os.path.isfile(path + "java.exe"): return path + "java.exe"
    if os.path.isfile(path + "bin" + os.sep + "java"): return path + "bin" + os.sep + "java"
    if os.path.isfile(path + "bin" + os.sep + "java.exe"): return path + "bin" + os.sep + "java.exe"

    if allow_sub_folders > 0:
        for sub in [path + sub for sub in os.listdir(path) if os.path.isdir(path + sub)]:
            result = __test_java_path(sub, allow_sub_folders - 1)
            if result is not None: return result
    else:
        return None


def get_simulator_path(user_suggested_path: Union[str, None] = None) -> Union[str, None]:
    """Trys to find the Simulator.jar.

    Args:
        user_suggested_path (Union[str, None], optiona): Path to check first. Default to None.

    Returns:
        Union[str, None]: Full path (including file name) to Simulator.jar or None if not found
    """
    path_list = []

    # User suggested path
    if user_suggested_path is not None: path_list.append(user_suggested_path)

    # Windows default installation locations
    path_list.append(os.path.expanduser('~') + os.path.sep + "AppData\\Roaming\\Warteschlangensimulator")
    path_list.append("C:\\Program Files (x86)\\Warteschlangensimulator")
    path_list.append("C:\\Program Files\\Warteschlangensimulator")

    # Search in home folder
    path_list.append(os.path.expanduser('~') + os.path.sep + "Warteschlangensimulator")
    path_list.append(os.path.expanduser('~') + os.path.sep + "Desktop" + os.path.sep + "Warteschlangensimulator")
    path_list.append(os.path.expanduser('~') + os.path.sep + "Documents" + os.path.sep + "Warteschlangensimulator")
    path_list.append(os.path.expanduser('~') + os.path.sep + "QS")
    path_list.append(os.path.expanduser('~') + os.path.sep + "Desktop" + os.path.sep + "QS")
    path_list.append(os.path.expanduser('~') + os.path.sep + "Documents" + os.path.sep + "QS")

    # Search in current folder
    path_list.append(os.path.abspath('.'))
    path_list.append(os.path.abspath('.') + os.path.sep + "Warteschlangensimulator")
    path_list.append(os.path.abspath('.') + os.path.sep + "QS")

    for path in path_list:
        if not path.endswith(os.path.sep): path += os.path.sep
        if os.path.isfile(path + "Simulator.jar"): return path + "Simulator.jar"

    return None
