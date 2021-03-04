
import java.util.*;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import petriNet.*;
import simplepdl.*;
import simplepdl.Process;

public class SimplePDLToPetriNet {
    
    static PetriNet pNet;
    static PetriNetFactory pnFactory;
    //map contenant les places en état 'finished'
    static Map<String, Place> finisheds = new HashMap<String, Place>();
    //map contenant les places en état 'started'
    static Map<String, Place> starteds = new HashMap<String, Place>();
    //map contenant les transitions de type 'start'
    static Map<String, Transition> starts = new HashMap<String, Transition>();
    //map contenant les transitions de type 'finish'
    static Map<String, Transition> finishs = new HashMap<String, Transition>();
    //map contenat les places de type ressource
    static Map<String, Place> placesResources = new HashMap<String, Place>();
    
    /*Traduire une workDefinition en des élèments de  réseau de Petri*/
    public static void convertWorkDef(WorkDefinition wd) {
        
        /* Création des places associées à la WorkDefinition */

        // La première pour le cas non commencé
        Place p_ready = pnFactory.createPlace();
        p_ready.setName(wd.getName() + "_ready");
        p_ready.setNbrJetons(1);
        pNet.getElements().add(p_ready);
        
        // La deuxième pour savoir si l'activité a été commencée
        Place p_started = pnFactory.createPlace();
        p_started.setName(wd.getName() + "_started");
        p_started.setNbrJetons(0);
        pNet.getElements().add(p_started);
        
        // La troisième pour le cas de  "l'activité est en cours"
        Place p_running = pnFactory.createPlace();
        p_running.setName(wd.getName() + "_running");
        p_running.setNbrJetons(0);
        pNet.getElements().add(p_running);
        
        // La dernière pour le cas de "l'activité est terminée "
        Place p_finished = pnFactory.createPlace();
        p_finished.setName(wd.getName() + "_finished");
        p_finished.setNbrJetons(0);
        pNet.getElements().add(p_finished);
        
        /* Création des transitions associées à la WorkDefinition */

        // La première pour commencer la WorkDefinition
        Transition t_start = pnFactory.createTransition();
        t_start.setName(wd.getName() + "_start");
        pNet.getElements().add(t_start);
        
        // La deuxième pour finir la WorkDefinition
        Transition t_finish = pnFactory.createTransition();
        t_finish.setName(wd.getName() + "_finish");
        pNet.getElements().add(t_finish);
        
        /* Création des arcs entre Places et Transitions associées à la WorkDefinition */
        
        Arc a_ready_start = pnFactory.createArc();
        a_ready_start.setSource(p_ready);
        a_ready_start.setTarget(t_start);
        a_ready_start.setNbrJetons(1);
        pNet.getArcs().add(a_ready_start);
        
        Arc a_start_started = pnFactory.createArc();
        a_start_started.setSource(t_start);
        a_start_started.setTarget(p_started);
        a_start_started.setNbrJetons(1);
        pNet.getArcs().add(a_start_started);
        

        Arc a_start_running = pnFactory.createArc();
        a_start_running.setSource(t_start);
        a_start_running.setTarget(p_running);
        a_start_running.setNbrJetons(1);
        pNet.getArcs().add(a_start_running);
        
        Arc a_running_finish = pnFactory.createArc();
        a_running_finish.setSource(p_running);
        a_running_finish.setTarget(t_finish);
        a_running_finish.setNbrJetons(1);
        pNet.getArcs().add(a_running_finish);
        
        Arc a_finish_finished = pnFactory.createArc();
        a_finish_finished.setSource(t_finish);
        a_finish_finished.setTarget(p_finished);
        a_finish_finished.setNbrJetons(1);
        pNet.getArcs().add(a_finish_finished);
        
        // Mettre à jour les maps
        starteds.put(wd.getName(), p_started);
        finisheds.put(wd.getName(), p_finished);
        starts.put(wd.getName(), t_start);
        finishs.put(wd.getName(), t_finish);
        //configurer les ressources alloués par une activité
        if (! wd.getResources().isEmpty()) {
        	List<Allocation> list = wd.getResources();
        	for (int i=0; i<list.size();i++ ) {
        		simplepdl.Resource resource_i = list.get(i).getResource();
        		int nbrJetons = list.get(i).getCount();
        		
                //arc depuis la place ressource vers la trasition start 

        		Arc a_resource_start = pnFactory.createArc();
                a_resource_start.setSource(placesResources.get(resource_i.getName()));
                a_resource_start.setTarget(starts.get(wd.getName()));
                a_resource_start.setNbrJetons(nbrJetons);
                pNet.getArcs().add(a_resource_start);
                
                //arc depuis la transition finish vers la place resource
                
                Arc a_finish_resource = pnFactory.createArc();
                a_finish_resource.setSource(finishs.get(wd.getName()));
                a_finish_resource.setTarget(placesResources.get(resource_i.getName()));
                a_finish_resource.setNbrJetons(nbrJetons);
                pNet.getArcs().add(a_finish_resource);
        	}
        }
    }
    /*Traduire une workSequence en des élèments de  réseau de Petri*/
    public static void convertWorkSeq(WorkSequence ws) {
        Arc a = pnFactory.createArc();
        a.setType(arcType.READ_ARC);
        a.setNbrJetons(1);
        if (ws.getLinkType() == WorkSequenceType.START_TO_START) {
            a.setSource(starteds.get(ws.getPredecessor().getName()));
            a.setTarget(starts.get(ws.getSuccessor().getName()));
        }
        else if(ws.getLinkType() == WorkSequenceType.START_TO_FINISH) {
            a.setSource(starteds.get(ws.getPredecessor().getName()));
            a.setTarget(finishs.get(ws.getSuccessor().getName()));
        }
        else if(ws.getLinkType() == WorkSequenceType.FINISH_TO_START) {
            a.setSource(finisheds.get(ws.getPredecessor().getName()));
            a.setTarget(starts.get(ws.getSuccessor().getName()));
        }
        else {
            a.setSource(finisheds.get(ws.getPredecessor().getName()));
            a.setTarget(finishs.get(ws.getSuccessor().getName()));
        }
        
        pNet.getArcs().add(a);
    }
    
    
    /* Traduire une Ressource en un motif sur le réseau de Petri*/
    public static void convertResource(simplepdl.Resource resource) {
    	Place p_resource = pnFactory.createPlace();
    	p_resource.setName(resource.getName());
    	p_resource.setNbrJetons(  resource.getOccur_nbr()) ;
    	placesResources.put(resource.getName(), p_resource);
    	pNet.getElements().add(p_resource);
    }
    public static void main(String[] args) {
    	
        /* Chargement des packages */
        SimplepdlPackage spdlPkgInst = SimplepdlPackage.eINSTANCE;
        PetriNetPackage pnPkgInst = PetriNetPackage.eINSTANCE;
        
        /* Chargement des ressources */
        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> m = reg.getExtensionToFactoryMap();
        m.put("xmi", new XMIResourceFactoryImpl());
                
        // Créer un objet resourceSetImpl qui contiendra le modèle
        ResourceSet resSet = new ResourceSetImpl();

        // Charger le modèle en entrée
        URI modelURI = URI.createURI("model/pdl-sujet.xmi");
        Resource resource = resSet.getResource(modelURI, true);
        
        // Récupération du Process (racine du SimplePDL)
        Process proc = (Process)resource.getContents().get(0);
        
        // Créer le modèle en sortie
        URI pnURI = URI.createURI("model/"+proc.getName()+"ToPetri.xmi");
        Resource pn = resSet.createResource(pnURI);
        
        // Créer 
        pnFactory = PetriNetFactory.eINSTANCE;
        pNet = pnFactory.createPetriNet();
        //pNet.setName(proc.getName());
        pn.getContents().add(pNet);
        
        System.out.println("Début de la conversion...");
        
        pNet.setName(proc.getName());
        for (Object o : proc.getProcessElements()) {
            if (o instanceof simplepdl.Resource) {
                convertResource((simplepdl.Resource)o);
            }
        }
        
        for (Object o : proc.getProcessElements()) {
            if (o instanceof WorkDefinition) {
                convertWorkDef((WorkDefinition)o);
            }
        }
        
        for (Object o : proc.getProcessElements()) {
            if (o instanceof WorkSequence) {
                convertWorkSeq((WorkSequence)o);
            }
        }
        
        try {
            pn.save(Collections.EMPTY_MAP);
        } catch(Exception e) {
            e.printStackTrace();
        }       
        System.out.println("Fin de la conversion !");
    }

}