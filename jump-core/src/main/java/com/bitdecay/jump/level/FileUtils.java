package com.bitdecay.jump.level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.swing.*;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.io.*;

public class FileUtils {

	public static String lastTouchedFileName = "";

	private static String lastTouchedDirectory = ".";

	private static final ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.enableDefaultTyping();
	}

	public static String toJson(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static String saveToFile(Object obj) {
		return saveToFile(toJson(obj));
	}

	public static String saveToFile(String json) {
		JFileChooser fileChooser = new JFileChooser(lastTouchedDirectory) {
			@Override
			protected JDialog createDialog(Component parent) throws HeadlessException {
				JDialog dialog = super.createDialog(parent);
				dialog.setAlwaysOnTop(true);
				dialog.setModalityType(ModalityType.APPLICATION_MODAL);
				dialog.setModal(true);
				return dialog;
			}
		};
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle("Save As");
		fileChooser.setApproveButtonText("Save");
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			lastTouchedFileName = fileChooser.getSelectedFile().getName();
			lastTouchedDirectory = fileChooser.getSelectedFile().getParent();
			try {
				FileWriter writer = new FileWriter(fileChooser.getSelectedFile());
				writer.write(json);
				writer.close();
				return json;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static <T> T loadFileAs(Class<T> clazz) {
		String file = loadFile();
		if (file == null || file.length() <= 0) {
			return null;
		} else {
			return loadFileAs(clazz, file);
		}
	}

	public static <T> T loadFileAs(Class<T> clazz, File file) {
		return loadFileAs(clazz, loadFile(file));
	}

	public static <T> T loadFileAs(Class<T> clazz, String json) {
		try {
			return mapper.readValue(json, clazz);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String loadFile() {
		JFileChooser fileChooser = new JFileChooser(lastTouchedDirectory);
		fileChooser.setApproveButtonText("Load");

		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			lastTouchedFileName = selectedFile.getName();
			lastTouchedDirectory = selectedFile.getParent();
			return loadFile(selectedFile);
		}
		return null;
	}

	public static String loadFile(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			StringBuffer json = new StringBuffer();
			String line = reader.readLine();
			while (line != null) {
				json.append(line);
				line = reader.readLine();
			}
			if (json.length() > 0) {
				return json.toString();
			} else {
				System.out.println("File was empty. Could not load.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}


	public static void setFileChooserWorkingDirectory(String workingDirectory){
        lastTouchedDirectory = workingDirectory;
        lastTouchedFileName = "";
    }
}
