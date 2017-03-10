import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class parser {
	public static HashMap<String, Integer> dictionnaire;
	public static HashMap<String, Integer> indexation;// a un mot associe l'index
	public static HashMap<Integer, String> indexationBis;// a un index associe le mot
	public static HashMap<Integer, HashMap<Integer, Integer> > mapDocMot; // recense les mots dans un doc
	public static HashMap<Integer, LinkedList<Integer> > mapMotDoc; // recense les documents dans lesquels apparait un mot
	
	public static int limiteLignes = 2000;
	public static int nbTopics = 50;
	
	public static String cheminAbsolu = "/user/6/rattanaa/3A/Traitement/PLSA/";
	public static String fileName = cheminAbsolu + "wiki.10k.massih.txt";
	public static String writenFile = cheminAbsolu + "dictionnaire_"+ limiteLignes +".txt";
	public static String IndexFile = cheminAbsolu + "indexation_"+ limiteLignes +".txt";
	public static String topicsFile = cheminAbsolu + "topic_" + limiteLignes + "_" + nbTopics + ".txt";

	
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
			indexationBis = new HashMap<>();
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
                		indexationBis.put(nombreMots, mot);
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
            mapMotDoc = new HashMap<>();
            int numeroDoc = 0;
            scanner = new Scanner(outputIndex);
            while ( scanner.hasNextLine() ){
            	// remplissage mapDocMot
            	ligne = scanner.nextLine();
            	numeroDoc++;
            	HashMap<Integer, Integer> mapDoc = new HashMap<>();
            	String[] mots = ligne.split(" ");
            	for ( int i = 0 ; i < mots.length ; i++ ){
            		String[] valeur = mots[i].split(":"); 
            		mapDoc.put(Integer.parseInt(valeur[0]), Integer.parseInt(valeur[1]));
            		
            		if ( !mapMotDoc.containsKey(valeur[0]) ){
            			LinkedList<Integer> docs = new LinkedList<>();
            			docs.add(numeroDoc);
            			mapMotDoc.put(Integer.parseInt(valeur[0]), docs);
            		} else {
            			mapMotDoc.get(Integer.parseInt(valeur[0])).add(numeroDoc);
            		}
            	}

            	mapDocMot.put(numeroDoc, mapDoc);
            }
            System.out.println("MApMotDoc et MapDocMot créés OK avec " + numeroDoc + " doc; testé OK");
            
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
            	for ( int doc = 1; doc <= limiteLignes; doc++ ) {
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
            	
            	//on MAJ les tableaux	
            	for ( int topic = 0; topic < nbTopics ; topic++ ){
	                	double denominateurTabTermeTopic = 0;
	                	
	                	//On calcule la valeur de denominateurTabTermeTopic
	                	for ( int doc = 1; doc <= limiteLignes ; doc++ ){
	                		for ( int mot : mapDocMot.get(doc).keySet()){
	                				int occurence =  mapDocMot.get(doc).get(mot);
	                				String key = doc + "-"  + mot + "-" + topic;
	                				double proba = probaTopicTermDoc.get(key);
	                				denominateurTabTermeTopic += occurence*proba;
	                		}
	                	}
	                	//On MAJ tabTermeTopic
	                	for ( int mot = 1 ; mot <= nombreMots ; mot++ ){
	                		double numerateur = 0;
	                		for ( int doc : mapMotDoc.get(mot) ){
	                				int occurence = mapDocMot.get(doc).get(mot);
	                				String key = doc + "-"  + mot + "-" + topic;
	                				double proba = probaTopicTermDoc.get(key);
	                				numerateur += occurence*proba;
	                		}
	                		tabTermeTopic[mot -1 ][ topic] = numerateur/denominateurTabTermeTopic;
	                	}
            	}

            		//On MAJ tabDocTopic
                	for ( int doc = 1 ; doc <= limiteLignes ; doc++ ){
                    	double denominateurTabDocTopic = 0;
                		//On calcul la valeur de denominateurTabDocTopic
            			for ( int topic = 0 ; topic < nbTopics ; topic++ ){
            				for ( int mot : mapDocMot.get(doc).keySet() ){
            					int occurence =  mapDocMot.get(doc).get(mot);
                				String key = doc + "-"  + mot + "-" + topic;
                				double proba = probaTopicTermDoc.get(key);
                				denominateurTabDocTopic += occurence*proba;
            				}
            			}

                		for ( int topic = 0; topic < nbTopics ; topic++){	
	                		double numerateur = 0;
	                		for ( int mot :  mapDocMot.get(doc).keySet() ){
	                				int occurence = mapDocMot.get(doc).get(mot);
	                				String key = doc + "-"  + mot + "-" + topic;
	                				double proba = probaTopicTermDoc.get(key);
	                				numerateur += occurence*proba;	                			
	                		}
	                		tabDocTopic[topic][doc-1] = numerateur/denominateurTabDocTopic;
                	}

            	}
            	//Les deux tableaux sont MAJ
                	System.out.println("Fin itération " + k);
            }
            //On est sorti de la boucle while/for
            //On va maintenant chercher la proba max pour chaque topic
            System.out.println("On va chercher les mots pour chaque topic");
            int[][] tabWordTopic = new int[5][nbTopics];
            double[] tabProba = new double[5];
            int[] tabMots = new int[5];
            double probaMin;
            int indexMin;
            for ( int word = 0 ; word < 5 ; word++){
            	for ( int topic = 0; topic < nbTopics; topic++){
            		tabWordTopic[word][topic] = 0;
            	}
            }
            
            for ( int topic = 0 ; topic < nbTopics ; topic++){
            	for ( int i = 0; i < 5; i++){
            		tabMots[i] = 0;
            		tabProba[i] = 0;
            	}
            	probaMin = 0;
            	indexMin = 0;
            	 for ( int word = 0 ; word < nombreMots ; word++ ){
            		 if ( tabTermeTopic[word][topic] > probaMin ){
            			 tabMots[indexMin] = word;
            			 tabProba[indexMin] = tabTermeTopic[word][topic];
            			 //MAJ du minimum
            			 indexMin = 0;
            			 probaMin = tabProba[indexMin];
            			 for ( int i = 1; i < 5 ; i++ ){
            				 if ( tabProba[i] < probaMin ){
            					 indexMin = i;
            					 probaMin = tabProba[i];
            				 }
            			 }
            		 }
            	 }
            	 
            	 for ( int i = 0; i < 5 ; i++){
            		 tabWordTopic[i][topic] = tabMots[i];
            	 }
            		
            }

            //on écrit le resultat en sortie
            writer = new PrintWriter(topicsFile);
            for ( int topic = 0 ; topic < nbTopics ; topic++ ){
            	writer.print("Topic " + topic + " : ");
            	for ( int word = 0 ; word < 5 ; word++ ){
            		writer.print(indexationBis.get(tabWordTopic[word][topic]) + " ");
            	}
            	writer.print("\n");
            }
            writer.close();
            System.out.println("fin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

		

	}
}
