/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ui.mjpeg;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.commons.math3.util.FastMath;

import tools.SetupData;

/**
 * Diese Klasse schreibt mit Hilfe mehrerer Hintergrund-Threads Bilder in eine temporäre Datei,
 * um später daraus eine MJPEG-Datei zu machen.
 * @author Alexander Herzog
 */
public class MJPEGMultiThreadBufferWriter extends MJPEGBufferWriterBase {
	private static final int MAX_THREADS=12; /* Damit uns der Arbeitsspeicher durch die vielen wartenden Bilder nicht ausgeht. */
	private final boolean storeAsJPEG;
	private final float quality;
	private final int maxThreads;
	private final List<WorkerThread> worker;
	private final List<BufferedImage> imageObjectCache;

	/**
	 * Konstruktor der Klasse
	 * @param storeAsJPEG	Bilder als jpeg speichern (<code>true</code>) oder als png (<code>false</code>)
	 * @param quality	Kompressionsqualität für jpegs (Wert zwischen 0 und 1)
	 */
	public MJPEGMultiThreadBufferWriter(final boolean storeAsJPEG, final float quality) {
		super();
		this.storeAsJPEG=storeAsJPEG;
		this.quality=quality;
		maxThreads=FastMath.min(MAX_THREADS,(SetupData.getSetup().useMultiCoreAnimation)?Runtime.getRuntime().availableProcessors()*2:1);
		worker=new ArrayList<>();
		imageObjectCache=new ArrayList<>();
	}

	private void writeToData() {
		while (worker.size()>0 && !worker.get(0).isAlive()) {
			worker.get(0).writeToDataOutputStream(tempOutputData);
			worker.remove(0);
		}
	}

	@Override
	public BufferedImage getImageObjectFromCache(final int width, final int height) {
		if (imageObjectCache.size()==0) return new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		return imageObjectCache.remove(0);
	}

	@Override
	public boolean addFrame(BufferedImage image, long timeStamp) {
		if (!isReady()) return false;
		frameCount++;

		writeToData();

		if (worker.size()>=maxThreads) try {
			worker.get(0).join();
		} catch (InterruptedException e) {}

		writeToData();

		worker.add(new WorkerThread(timeStamp,image));

		return true;
	}

	@Override
	public boolean doneFrames() {
		if (!isReady()) return false;

		while (worker.size()>0) {
			try {
				worker.get(0).join();
			} catch (InterruptedException e) {}

			writeToData();
		}

		imageObjectCache.clear();

		return true;
	}

	private class WorkerThread extends Thread {
		private BufferedImage image;
		private final long timeStamp;
		private ByteArrayOutputStream buffer;

		public WorkerThread(final long timeStamp, final BufferedImage image) {
			super("Image encoder");
			this.timeStamp=timeStamp;
			this.image=image;
			start();
		}

		@Override
		public void run() {
			buffer=new ByteArrayOutputStream();
			try {
				if (storeAsJPEG && quality>0) {
					final ImageWriter jpgWriter=ImageIO.getImageWritersByFormatName("jpeg").next();
					final ImageWriteParam jpgWriteParam=jpgWriter.getDefaultWriteParam();
					jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					jpgWriteParam.setCompressionQuality(quality);
					try(ImageOutputStream outputImage=new MemoryCacheImageOutputStream(buffer)) {
						jpgWriter.setOutput(outputImage);
						jpgWriter.write(null,new IIOImage(image,null,null),jpgWriteParam);
						jpgWriter.dispose();
					}
				} else {
					ImageIO.write(image,storeAsJPEG?"jpg":"png",buffer);
				}
			} catch (IOException e) {}
		}

		public boolean writeToDataOutputStream(final DataOutputStream data) {
			if (imageObjectCache.size()<=MAX_THREADS) imageObjectCache.add(image);
			image=null;

			try {
				tempOutputData.writeLong(timeStamp);
				tempOutputData.writeInt(buffer.size());
				tempOutputData.write(buffer.toByteArray());
				bytesCount+=4+8+buffer.size();
			} catch (IOException e) {
				return false;
			}
			return true;
		}
	}
}