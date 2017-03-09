import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

public class parser {
	public static HashMap<String, Integer> dictionnaire;
	public static HashMap<String, Integer> indexation;
	
	public static String fileName = "/user/6/rattanaa/3A/Traitement/wiki.10k.massih.txt";
	public static String testFile = "/user/6/rattanaa/3A/Traitement/test.txt";
	public static String writenFile = "/user/6/rattanaa/3A/Traitement/dictionnaire.txt";
	public static String IndexFile = "/user/6/rattanaa/3A/Traitement/indexation.txt";
	public static int limiteLignes = 1000;
	
	
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
            
            
            
            // créer le fichier contenant le vocabulaire
            PrintWriter writerBis = new PrintWriter(fileToWrite);
            writerBis.print("");
            for (String key: dictionnaire.keySet()){
            	int value = dictionnaire.get(key);
            	writerBis.print(key+":"+value+"\n");
            }
            writerBis.close();
            System.out.println("Vocabulaire OK");
            


            
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

		

	}
}
