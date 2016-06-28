package com.funmix.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import com.funmix.common.ImageComparer;
import com.funmix.common.Log4jLogger;

public class ImgCompareUtils {
	protected static Logger log = Log4jLogger.getLogger(ImgCompareUtils.class);
	private double matchPoint = 0.995;
	private HashMap<String, BufferedImage> imgStore = new HashMap<String, BufferedImage>();
	private BufferedImage imgVCode;

	public void setImgVCode(BufferedImage imgVCode) {
		this.imgVCode = imgVCode;
	}

	public ImgCompareUtils(String device) {
		try {
			String appPath = Utils.getCurrentPath() + "//";
			String workDir = appPath + device;

			if (new File(workDir).exists() == false) {
				FileUtils.forceMkdir(new File(workDir));
			}

			imgStore.put("imgNeedVCode", ImageUtils.getImage(appPath + "img//codedra.png"));
			imgStore.put("imgFail", ImageUtils.getImage(appPath + "img//qqfail.png"));
			imgStore.put("imgFail2", ImageUtils.getImage(appPath + "img//qqfail2.png"));
			imgStore.put("imgFail3", ImageUtils.getImage(appPath + "img//qqfail3.png"));
			imgStore.put("imgSucc", ImageUtils.getImage(appPath + "img//qqsucc.png"));
			imgStore.put("imgSucc1", ImageUtils.getImage(appPath + "img//qqsucc1.png"));
			imgStore.put("imgSucc2", ImageUtils.getImage(appPath + "img//qqsucc2.png"));
			imgStore.put("imgSucc3", ImageUtils.getImage(appPath + "img//qqsucc3.png"));
			imgStore.put("imgActFail", ImageUtils.getImage(appPath + "img//actfail.png"));
			imgStore.put("imgAuthGame", ImageUtils.getImage(appPath + "img//authdra.png"));

		} catch (IOException e) {
			log.error("init ImgCompareUtils failed!",e);
		}
	}

	public boolean compare(String keyImage, BufferedImage splitImage) {
		BufferedImage keyImg;
		if(keyImage.equals("imgVCode"))
			keyImg = imgVCode;
		else
			keyImg = imgStore.get(keyImage);
		if(keyImg!=null){
			ImageComparer imageCom = new ImageComparer(keyImg, splitImage);
			//System.out.println(imageCom.modelMatch());
			if (imageCom.modelMatch() > matchPoint) {
				return true;
			}
		}else{
			if(!keyImage.equals("imgVCode"))
				log.error("Error:keyImg not found!" + keyImage);
		}
		return false;
	}
}
