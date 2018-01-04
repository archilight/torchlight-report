package com.torchlight.report;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.opensagres.xdocreport.document.images.FileImageProvider;
import fr.opensagres.xdocreport.document.images.IImageProvider;

public class DocXElement {
	private  String name = "(undefined)";
	private  String documentation = "(undefined)";
	private  List<String> connection ;
	private  List<DocXElement> children;
	private  List<DocXElement> elements;
	private File image = new File(TorchLightPlugin.INSTANCE.getPluginFolder().getAbsolutePath()+"/torchlight.jpg");

	public DocXElement(String name, String documentation) {
		this.name = name;
		this.documentation = documentation;	
		this.connection = new ArrayList<String>();
		this.children = new ArrayList<DocXElement>();
		this.elements = new ArrayList<DocXElement>();
	}
	
	public String getDocumentation() {
		return documentation;
	}
	
	public void addConnection(String elem) {
		connection.add(elem);
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getConnectin() {
		return connection;
	}

	public List<DocXElement> getChildren() {
		return children;
	}
	
	public void addChild(DocXElement child) {
		children.add(child);
	}
	
	public void setChildren(List<DocXElement> children) {
		this.children = children;
	}

	//@FieldMetadata( images = { @ImageMetadata( name = "image" ) } )
	
	
	public IImageProvider getImage() {
		// resize image proportionally to width as set below 
		System.out.println("asked: " + name + " "+ image);
		IImageProvider picture = new FileImageProvider(this.image, true);
        picture.setUseImageSize(true);
        picture.setWidth(550f);  //TODO - set via preferences
        picture.setResize(true);
        return picture;
	}

	public void setImage(File image) {
		System.out.println("set: " + name + " "+ image);
		this.image = image;
	}
	
	
	

	public List<DocXElement> getElements() {
		return elements;
	}

	public void setElements(List<DocXElement> elements) {
		this.elements = elements;
	}
	
	public void addElement(DocXElement element) {
		this.elements.add(element);
	}
}
