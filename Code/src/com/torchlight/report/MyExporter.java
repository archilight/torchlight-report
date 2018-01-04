/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.torchlight.report;

import java.io.File;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

//import com.archimatetool.canvas.model.ICanvasModel;
import com.archimatetool.editor.model.IModelExporter;
import com.archimatetool.model.IArchimateModel;

import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

/**
 * Torchlight Export to .docx file
 * 
 * @author Jeff Parker
 */
public class MyExporter implements IModelExporter {
    
    
	String MY_EXTENSION = ".docx"; //$NON-NLS-1$
    String MY_EXTENSION_WILDCARD = "*.docx"; //$NON-NLS-1$
    int DEBUG = 1;
    IContext context;
    File targetImageFolder;
    FieldsMetadata metadata;
    
    
    public void debug(String msg)
    {
    	if (DEBUG == 1)
    		System.out.println(msg);
    }
    
    
    
    public MyExporter() {
    }

    @Override
    public void export(IArchimateModel model) {  
    	//debug("export document");
    	
    	//File plugin = TorchLightPlugin.INSTANCE.getPluginFolder();
    	
    	//debug("Plugin="+plugin.getAbsolutePath());
    	
    	
    	
    	File tempFile = TorchLightPlugin.INSTANCE.getDefaultUserTempFolder();
    	//debug("temp="+tempFile.getPath());
    	
    	
    	targetImageFolder = new File( tempFile,"/images");
        targetImageFolder.mkdirs();
        
        ReportSelectionDialog dialog = new ReportSelectionDialog(Display.getCurrent().getActiveShell(), model);
        dialog.create();
        if (dialog.open() == Window.OK) {
            //System.out.println("ok pressed");
           
        }
    }   
}
