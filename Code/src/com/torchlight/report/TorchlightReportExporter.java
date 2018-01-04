package com.torchlight.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import com.archimatetool.editor.diagram.util.DiagramUtils;
import com.archimatetool.editor.diagram.util.ModelReferencedImage;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IDiagramModelArchimateConnection;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IDiagramModelObject;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.INameable;
import com.archimatetool.model.IProperty;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import fr.opensagres.xdocreport.template.formatter.NullImageBehaviour;



/**
 * Torchlight Reports export class
 * 
 * @author Jeff Parker
 */
public class TorchlightReportExporter {
	

	private IArchimateModel fModel;
	private File fExportFileName;
	private File fMainTemplateFile;
	private File targetImageFolder;
	private String result = "";
	private boolean error = false;
	
	boolean DEBUG = true;
	
	IContext context;
	FieldsMetadata metadata;
	
	/**
     * Export model to Torchlight Reports
     * @param model             The ArchiMate model
     * @param exportFileName    The report file name
     * @param mainTemplateFile  The template file to be used
     */
	
    public TorchlightReportExporter(IArchimateModel model, File exportFileName, File templateFileName ) {
        fModel = model;
        fExportFileName = exportFileName;
        fMainTemplateFile = templateFileName;
        
    }
    
    public void debug(String msg)
    {
    	if (DEBUG)
    		System.out.println(msg);
    }
    
    public String result() {
    	return result;
    }
    
    public boolean error() {
    	return error;
    }
    
    public void Run() throws IOException {
        
        try {
        
        //System.out.println("Exporting: " + fModel.getName());
        
        // reset
        error = false;
        
        // create temporary folder to contain the images
        File tempFile = TorchLightPlugin.INSTANCE.getDefaultUserTempFolder();
    	targetImageFolder = new File( tempFile,"/images");
        targetImageFolder.mkdirs();
        
        // --------------------------------------------------------------------------------------------------
        // 1) open template file Freemarker template engine and cache
        // it to the registry
    	     
        // open template stream
        InputStream r = new FileInputStream(fMainTemplateFile);
        IXDocReport report = XDocReportRegistry.getRegistry().loadReport( r , TemplateEngineKind.Freemarker );

        //------------------------------------------------------------------------------------------------------
        // 2) Create fields metadata to manage lazy loop ([#list Freemarker) for table row.
        metadata = report.createFieldsMetadata();
        metadata.load("top", DocXElement.class);
        metadata.setBehaviour(NullImageBehaviour.RemoveImageTemplate);
        metadata.addFieldAsImage("image1", "child1.image");
        metadata.addFieldAsImage("image2", "child2.image");
        metadata.addFieldAsImage("image3", "child3.image");
        metadata.addFieldAsImage("image4", "child4.image");
        
        
        //metadata.load("servicedefinitions", ServiceDefinition.class);
        //metadata.load("service", ServiceDefinition.class); 
        //metadata.addFieldAsImage("decompositionimage", "service.decompositionimage");
        
       
        //metadata.addFieldAsImage("photo");
        //metadata.addFieldAsImage("photo2");
       
        
        //------------------------------------------------------------------------------------------------------
        // 3) Create the context java model
        context = report.createContext();
        
        context.put("modelname", fModel.getName()); 
        context.put("modelpurpose", fModel.getPurpose());

        // write out archimate objects
        for(EObject objectToExport : fModel.getFolders()) {
        	String name = ((INameable) objectToExport).getName();
            //debug(objectToExport.getClass().getName()+ " " + ((INameable) objectToExport).getName()+"\n");
            if (name.equals("Views")) {
            	IFolder viewFolder = (IFolder) objectToExport;
            	writeFolders(viewFolder);
            }
        }
        
        //------------------------------------------------------------------------------------------------------
        // 4) Generate report by merging Java model with the Docx
        // get destination file
        OutputStream writer = new FileOutputStream( fExportFileName );
        report.process( context, writer );
        writer.close();
        
        ///----------------------------------------------------------------
            
        //debug("Finished");
        result = "Content generated into "+ fExportFileName.getName();
        }
        
        // handle exceptions
        catch ( IOException e )
        {
        	// show dialog with template error
       	 	MultiStatus status = createMultiStatus(e.getLocalizedMessage(),e);
            ErrorDialog.openError(Display.getCurrent().getActiveShell(), "IO Error", "Some kind of error", status);
            result = e.toString();
            error = true;
       	 	e.printStackTrace();
        }
        catch ( XDocReportException e )
        {
            // show dialog with template error
            MultiStatus status = createMultiStatus(e.getLocalizedMessage(),e);
            ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Template Error", "Error in template file", status);
            result = e.toString();
            error = true;
            e.printStackTrace();
        }
    }
    
    // Report on all the folders of interest for reporting. 
    private void writeFolders(IFolder folder) throws IOException
    {
    	
    	List <MyObject> docTree = new ArrayList <MyObject>();
    	
    	populateTree(folder,docTree, "");
    	//printTree(docTree,"");
    	
    	DocXElement docx = new DocXElement("top","");
    	populateOutput( docTree,  docx);
    	context.put("top",docx);	
    }
    
    // recusively walk through archimate view folder and fill out myObject with elements
    private void populateTree(IFolder folder, List <MyObject> docTree, String tab) {
    	String name = folder.getName();
    	MyObject element = new MyObject(folder, name, true );
    	docTree.add(element);
		
    	// do all folders
    	for(IFolder f : folder.getFolders()) {
            populateTree(f, element.getChildren(), tab + ".");
        }
    	
    	// do all elements
        for(EObject object : folder.getElements()) {
        		IDiagramModel diagram = (IDiagramModel) object;   		
        		String name2 = diagram.getName();
        		element.addChild(new MyObject(object, name2, false));
        }
        
        // sort elements alphabetically  
        element.mysort();  
    }
    
    // used for debugging
    private void printTree(List <MyObject> docTree, String tab) {			
    	for(MyObject leaf : docTree) {
    		debug(tab+leaf.getName());
    		printTree(leaf.getChildren(), tab + ".");
        }    
    }
    
    // walk through tree and output content
    private void populateOutput(List<MyObject> docTree, DocXElement docx) {
    	DocXElement view, thisDocXElement;	
    	String documentation;
    	File imageFile = null;
    	IDiagramModel diagram;
    	IDiagramModelArchimateObject elem;
    	String elemClass, elemName, elemDocumentation, torchlightClass;
    	String torchlightName, torchlightImage, torchlightTree;
    	
    	
    	for(MyObject leaf : docTree) {
    		torchlightClass = "";
    		if (leaf.getbFolder()) { // true if folder
    			
    			//------------ add folder ---------------------------------------------------------------
    			documentation = ((IFolder) leaf.getObject()).getDocumentation();
    			imageFile = new File("ffobar"); //TorchLightPlugin.INSTANCE.getPluginFolder().getAbsolutePath()+"/blank.png");
    			view = new DocXElement(leaf.getName(),documentation);
    			torchlightName = getProperty("#name", ((IFolder) leaf.getObject()).getProperties());
    			torchlightImage = "";
    			torchlightTree = getProperty("#torchlight", ((IFolder) leaf.getObject()).getProperties());
    		}
    		else  // not folder so diagram
    		{
    			//------------ add diagram object -------------------------------------------------------
    			// diagram
    			diagram = ((IDiagramModel) leaf.getObject());
    			torchlightName = getProperty("#name", diagram.getProperties());
    			torchlightImage = getProperty("#image", diagram.getProperties());
    			torchlightTree = "";
    			documentation = diagram.getDocumentation();
    			view = new DocXElement(leaf.getName(),documentation);
    			
    			// filter by #element class value if set
    			torchlightClass = getProperty("#element", diagram.getProperties());
    			
    			//------------ add any elements ---------------------------------------------------------
    			for (IDiagramModelObject obj: diagram.getChildren()) {
	    			if (obj instanceof IDiagramModelArchimateObject) {
	        			// process diagram object
	        			elem = (IDiagramModelArchimateObject) obj;
	        			elemClass = elem.getArchimateElement().eClass().getName();
	        			//debug("writeView:class:"+elemClass);
	        			if (elemClass.contains(torchlightClass)) {  // apply filter if set
	        				// grab the elements
	        				elemName = elem.getName();
	        				elemDocumentation = elem.getArchimateConcept().getDocumentation();
	        				    				
	        				// add process to service definition object
	        				thisDocXElement = new DocXElement(elemName, elemDocumentation);
	        				
	        				//debug("\tname="+elemName+",documentation="+elemDocumentation);	
	        				
	        				// look at relationships
	        				for (EObject connector: elem.getSourceConnections()) {
	        					// look for things connect to this service, should just be locations
	        					if (connector instanceof IDiagramModelArchimateConnection) {
	        						IDiagramModelArchimateConnection connection = (IDiagramModelArchimateConnection) connector;
	        						IDiagramModelArchimateObject source = (IDiagramModelArchimateObject) connection.getSource();
	        						IDiagramModelArchimateObject target = (IDiagramModelArchimateObject) connection.getTarget();
	        						String connectorClass = connection.getClass().getName();
	        						String sourceName = source.getArchimateElement().getName();
	        						String targetName = target.getArchimateElement().getName();
	        						//debug("writeView:connector:"+sourceName+"<-"+elemName+"->"+targetName);
	        						thisDocXElement.addConnection(targetName);
	        					}
	        				}
	        				
	        				// add the element
	        				view.addElement(thisDocXElement);
	        			}
	        		}		
    			}	
    			//------------ add image object -------------------------------------------------------
    			
    			// write image
    			ModelReferencedImage geoImage = DiagramUtils.createModelReferencedImage(diagram, 1, 10);
            	String diagramName = diagram.getId()+".png";
            	Image image = geoImage.getImage();
            	                
                try {
                    ImageLoader loader = new ImageLoader();
                    loader.data = new ImageData[] { image.getImageData() };
                    imageFile = new File(targetImageFolder, diagramName);
                    loader.save(imageFile.getAbsolutePath(), SWT.IMAGE_PNG);
                }
                finally {
                    image.dispose();
                }
    		}		
    		
    		// create view object
    		view.setImage(imageFile);   // add image   		
    		
    		// add to the diagram set
    		docx.addChild(view);
    		
    		// process the children of this object (should only happen for folders)
    		populateOutput(leaf.getChildren(), view);
    		
    		// output to any specific properties
    	    //   #name markup name to use
    	    //	 #image name to store the image of the diagram
    		if ( torchlightName.length() > 0 ) {
        		//debug("#name set for "+view.getName());
        		
        		context.put(torchlightName+".name", view.getName());
            	context.put(torchlightName+".documentation", view.getDocumentation());
            	
            	if (view.getElements().size() > 0) {
            		// output the process array
            		//debug("#elements for"+ torchlightName+".elements");
            		context.put(torchlightName+".elements",view.getElements());
            	}
            	
            	if (torchlightImage.length() > 0) {
        			// write out the image metadata and content
        			//debug("#image set for "+view.getName());
                    metadata.addFieldAsImage(torchlightImage);
                    context.put(torchlightImage, view.getImage());
        		}
            	
            	if ( torchlightTree.length() > 0 ) {
            		// write out the tree from this point
        			//debug("#torchlighttree set for "+view.getName());
        			context.put(torchlightTree, view);
        		}
            	
        	} 		
        } 
    }
    
    
    // helper to return property key value
    // @return String - "" if key not found
    private String getProperty(String key, EList<IProperty> properties) {
    	for (IProperty prop: properties){
    		if (prop.getKey().contains(key)){
    			return prop.getValue();
    		}
		}
    	return "";
    }
    
    // helper for exception handling
    // @return MultiStatus for error handling
    private static MultiStatus createMultiStatus(String msg, Throwable t) {

        List<Status> childStatuses = new ArrayList<>();
        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

        for (StackTraceElement stackTrace: stackTraces) {
            Status status = new Status(IStatus.ERROR,
                    "com.torchlight.report", stackTrace.toString());
            childStatuses.add(status);
        }

        MultiStatus ms = new MultiStatus("com.torchlight.report",
                IStatus.ERROR, childStatuses.toArray(new Status[] {}),
                t.toString(), t);
        return ms;
    }
}
