import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class parser {
	public static HashMap<String, Integer> dictionnaire;
	public static HashMap<String, Integer> indexation;
	public static HashMap<Integer, HashMap<Integer, Integer> > mapDocMot;
	
	public static int limiteLignes = 1000;
	public static int nbTopics = 20;
	
	public static String cheminAbsolu = "/user/6/rattanaa/3A/Traitement/PLSA/";
	public static String fileName = cheminAbsolu + "wiki.10k.massih.txt";
	public static String writenFile = cheminAbsolu + "dictionnaire_"+ limiteLignes +".txt";
	public static String IndexFile = cheminAbsolu + "indexation_"+ limiteLignes +".txt";

	
	public static void main(String [] args){
		 String ligne = null;
		 File inputFile = new File(fileName);
		 File fileToWrite = new File(writenFile);
		 File outputIndex = new File (IndexFile);
		 int nombreMots = 0;
		try {
			/**************** Question 1 & 2 ****************************/
			
			
			dictionnaire = new HashMap<>();
			indexation = new HashMap<>();
			int compteurLigne = 0;
            Scanner scanner = new Scanner(inputFile);
            PrintWriter writer = new PrintWriter(outputIndex);
            int indexMot = 0;

            
            
            while (scanner.hasNextLine() && compteurLigne < limiteLignes ) {
                ligne = scanner.nextLine();
                //On récupère chaque mot de la ligne
                String[] mots = ligne.split(" ");
                HashMap<Integer,Integer> tmpMap = new HashMap<>(); //Va nous servir a compter les mots dans un document

                for ( String mot : mots){
                	int value = 1;
                    int valueDoc = 1; 
                    //On remplit le dictionnaire et la Map d'indexation
                	if ( dictionnaire.containsKey(mot) ){
                		value = dictionnaire.get(mot) + 1;
                	} else {
                		nombreMots++;
                		indexation.put(mot, nombreMots);
                	}
            		dictionnaire.put(mot, value);      	
                	
                	//On remplit la Map qui va nous permettre de compter le nombre de mots par documents
            		indexMot = indexation.get(mot);              	
                	if ( tmpMap.containsKey(indexMot)){
                		valueDoc = tmpMap.get(indexMot) + 1;
                	}
                		tmpMap.put(indexMot, valueDoc);

                }
                
                
                //tmpMap est maintenant rempli et on va ecrire dans le fichier indexation.txt
                for (Integer key: tmpMap.keySet()){
                	int value = tmpMap.get(key);
                	writer.print(key+":"+value+" ");
                }
                writer.print("\n");
                //On vide la Map à la fin de la ligne
                tmpMap.clear();
                compteurLigne++;
            }
            
            writer.close();
            
            System.out.println("Indexation OK");
            
            // créer le fichier contenant le vocabulaire
            PrintWriter writerBis = new PrintWriter(fileToWrite);
            writerBis.print("");
            for (String key: dictionnaire.keySet()){
            	int value = dictionnaire.get(key);
            	writerBis.print(key+":"+value+"\n");
            }
            writerBis.close();
            System.out.println("Vocabulaire OK");
            
            
            
            
            /**************** Question 3 ****************************/
            
            //Créer hashmap recensant les mots pour chaque doc
            mapDocMot = new HashMap<>();
            int numeroDoc = 0;
            scanner = new Scanner(outputIndex);
            while ( scanner.hasNextLine() ){
            	ligne = scanner.nextLine();
            	numeroDoc++;
            	HashMap<Integer, Integer> mapDoc = new HashMap<>();
            	String[] mots = ligne.split(" ");
            	for ( int i = 0 ; i < mots.length ; i++ ){
            		String[] valeur = mots[i].split(":"); 
            		mapDoc.put(Integer.parseInt(valeur[0]), Integer.parseInt(valeur[1]));
            	}

            	mapDocMot.put(numeroDoc, mapDoc);
            }
            System.out.println("Tableau Doc Mots créé OK avec " + numeroDoc + " doc; testé OK");
            
            //Initialisation de la matrice termes-topics
            double[][] tabTermeTopic = new double[nombreMots][nbTopics];
            double[][] tabDocTopic = new double[nbTopics][limiteLignes];
            Random r = new Random();
        	for ( int j = 0 ; j < nbTopics ; j++ ){
        		for ( int i = 0 ; i < nombreMots ; i++ ){
            		tabTermeTopic[i][j] = r.nextDouble();
            	}
            	for ( int k = 0 ; k < limiteLignes ; k++ ){
            		tabDocTopic[j][k] = r.nextDouble();
            	}
            }
            
            
            System.out.println("Tableaux proba initialisés OK");
            
            //On commence le calcul
            //On fait ici une boucle for mais il faudrait faire un while
            for ( int k = 0 ; k < 25; k ++){
            	//On commence par stocker les P(z|d;t) dans une hashmap
            	// La clé sera sous la forme : doc-terme-topic
            	HashMap<String, Double> probaTopicTermDoc = new HashMap<>();
            	for ( int doc = 1; doc <= 1000; doc++ ) {
            		for ( int mot : mapDocMot.get(doc).keySet() ){
            			double denominateur = 0;
            			for ( int topic = 0; topic < nbTopics; topic++){
            				denominateur += tabTermeTopic[mot-1][topic]*tabDocTopic[topic][doc-1]; 
            			}
            			for( int topic = 0; topic < nbTopics; topic++){
            				String key = doc + "-" + mot + "-" + topic;
            				double value = tabDocTopic[topic][doc-1]*tabTermeTopic[mot-1][topic]/denominateur;
            				probaTopicTermDoc.put(key, value);
            			}
            		}
            	}
            	
            	System.out.println("Tableau des P(z|d;t) créé à l'itération " + k);
            	
            	//on MAJ les tableaux	
            	for ( int topic = 0; topic < nbTopics ; topic++ ){
                	double denominateurTabTermeTopic = 0;
                	System.out.println("topic numero : " + topic);
                	
                	//On calcule la valeur de denominateurTabTermeTopic
                	for ( int doc = 1; doc <= limiteLignes ; doc++ ){
                		for ( int mot = 1; mot <= nombreMots ; mot ++){
                			if ( mapDocMot.get(doc).containsKey(mot) ){
                				int occurence =  mapDocMot.get(doc).get(mot);
                				String key = doc + "-"  + mot + "-" + topic;
                				double proba = probaTopicTermDoc.get(key);
                				denominateurTabTermeTopic += occurence*proba;
                			}
                		}
                	}
                	
                	//On MAJ tabTermeTopic
                	for ( int mot = 1 ; mot <= nombreMots ; mot++ ){
                		double numerateur = 0;
                		for ( int doc : mapDocMot.keySet() ){
                			if ( mapDocMot.get(doc).containsKey(mot) ){
                				int occurence = mapDocMot.get(doc).get(mot);
                				String key = doc + "-"  + mot + "-" + topic;
                				double proba = probaTopicTermDoc.get(key);
                				numerateur += occurence*proba;
                			}
                		}
                		tabTermeTopic[mot -1 ][ topic] = numerateur/denominateurTabTermeTopic;
                	}
 
            		//On MAJ tabDocTopic
                	for ( int doc = 1 ; doc <= limiteLignes ; doc++ ){
                		
                		//On calcul la valeur de denominateurTabDocTopic
                    	double denominateurTabDocTopic = 0;
                    	for ( int topicBis = 0 ; topicBis < nbTopics ; topicBis++ ){
                    		for ( int mot = 1 ; mot <= nombreMots ; mot++ ){
                    			if ( mapDocMot.get(doc).containsKey(mot) ){
                    				int occurence =  mapDocMot.get(doc).get(mot);
                    				String key = doc + "-"  + mot + "-" + topicBis;
                    				double proba = probaTopicTermDoc.get(key);
                    				denominateurTabTermeTopic += occurence*proba;
                    			}
                    		}
                    	}
                		
                		double numerateur = 0;
                		for ( int mot = 1 ; mot <= nombreMots ; mot++ ){
                			if ( mapDocMot.get(doc).containsKey(mot) ){
                				int occurence = mapDocMot.get(doc).get(mot);
                				String key = doc + "-"  + mot + "-" + topic;
                				double proba = probaTopicTermDoc.get(key);
                				numerateur += occurence*proba;
                			}
                		}
                		tabDocTopic[topic][doc-1] = numerateur/denominateurTabDocTopic;
                	}
            	}
            	//Les deux tableaux sont MAJ
            	System.out.println("les tableaux ont été MAJ a l'itération :" + k);

            }
            //On est sorti de la boucle while/for
            //On va maintenant chercher la proba max pour chaque topic
            System.out.println("On va chercher les mots pour chaque topic");
            
            

            
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

		

	}
}
