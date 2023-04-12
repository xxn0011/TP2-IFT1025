package client;

import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    private Socket clientSocket;
    private ServerSocket server;
    private ObjectInputStream in;
    private ObjectOutputStream out;


    public static void main(String[] args) {
        try {
            // Connect to the server

            Socket ClientSocket = new Socket("127.0.0.1", 1338);

            // Create input and output streams
            //OutPutStreamWriter ?? Le server envoie des objets
            ObjectOutputStream out = new ObjectOutputStream(ClientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(ClientSocket.getInputStream());

            //Envoie au server les lignes tapées sur la console.
            Scanner scanner = new Scanner(System.in);

            //On récupère les commandes entrées par l'utilisateur
            //faire boucler le scanner afin que le client entre ses choix successivement.
            String command = scanner.nextLine();

            // Send event command to the server
            //envoyer les commandes au server
            //out.writeObject("CHARGER");
            //out.writeObject("INSCRIRE");
            //out.flush();
            //in.readObject().

            scanner.close();
            // Receive response from the server
            String response = (String)in.readObject();
            System.out.println("Response from server: " + response);

            // Close the socket and streams
            out.close();
            in.close();
            ClientSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Client(){
        Scanner scanner = new Scanner(System.in);

        // accueil bienvenue au portal
        System.out.println("*** Bienvenue au portail d'inscription de cours de l'UDEM *** \n");

        Boolean go = true;
        while(go){

            System.out.println("Veuillez choisir la session pour laquelle vous voulez consulter la liste des cours: \n" +
                    "1. Automne \n" +
                    "2. Hiver\n" +
                    "3. Ete\n" +
                    "> Choix: ");

            String command = scanner.nextLine();
            handleSwitchSessions(command);
            ArrayList<Course> courseListFiltered = handleSwitchSessions(command);

            System.out.println("> Choix: " +
                    "1. Consulter les cours offerts pour une autre session \n" +
                    "2. Inscription a un cours\n" );

            command = scanner.nextLine();
            if(command == "1") {
                continue;
            }
            else {
                System.out.println("Veuillez saisir votre prénom: ");
                String prenom = scanner.nextLine();
                System.out.println("Veuillez saisir votre nom: ");
                String nom = scanner.nextLine();
                System.out.println("Veuillez saisir votre email: ");
                String email = scanner.nextLine();
                System.out.println("Veuillez saisir votre matricule: ");
                String matricule = scanner.nextLine();
                System.out.println("Veuillez saisir le code du cours: ");
                String code = scanner.nextLine();
                String session = courseListFiltered.get(0).getSession();
                String coursNom = getCoursName(courseListFiltered, code);
                inscription(code, coursNom, session, nom, prenom, email, matricule);
                go = false;
            }
        }

        scanner.close();
    }

    //une première fonctionnalité qui permet au client de récupérer la liste des
//cours disponibles pour une session donnée. Le client envoie une requête charger
//au serveur. Le serveur doit récupérer la liste des cours du fichier cours.txt et
//l’envoie au client. Le client récupère les cours et les affiche.
    public ArrayList<Course> getCoursList(String session){
        try{
            out.writeObject("CHARGER " + session);
            ArrayList<Course> coursList = (ArrayList<Course>) in.readObject();

            in.close();
            out.close();

            return coursList;

        } catch (ConnectException connectException){
            System.out.println("erreur: probleme de connexion");
        } catch (IOException ioException){
            System.out.println("erreur: ioexception");
        } catch (ClassNotFoundException classNotFoundException){
            System.out.println("erreur: classNotFoundException");
        }

        return null;
    }

    public void inscription(String codeCours,String nomCours,String session,
                            String nom, String prenom, String email, String matricule){

        try {
            //On créé le cours auquel veut s'inscrire l'étudiant
            Course course = new Course(codeCours, nomCours, session);

            //Envoyer commande au server.
            out.writeObject("INSCRIRE");
            RegistrationForm form = new RegistrationForm(nom, prenom, email, matricule, course);
            out.writeObject(form);
            out.flush();

            //On affiche ce que retourne la méthode handleRegistration
            System.out.println(in.readObject());

        } catch (IOException e) {
            System.err.println("Erreur d'entrée/sortie : " + e.getMessage());
        } catch(ClassNotFoundException e){
            System.err.println("Classe non trouvée : " + e.getMessage());
        }
    }

    public ArrayList<Course> handleSwitchSessions(String choix){

        ArrayList<Course> courseListFiltered;

        switch (choix) {
            case "1":
                System.out.println("Les cours offerts pendant la session d'automne sont: \n");
                courseListFiltered = getCoursList("Automne");
                printListCourses(courseListFiltered);
                return courseListFiltered;
            case "2":
                System.out.println("Les cours offerts pendant la session d'hiver sont: \n");
                courseListFiltered = getCoursList("Hiver");
                printListCourses(courseListFiltered);
                return courseListFiltered;
            case "3":
                System.out.println("Les cours offerts pendant la session d'ete sont: \n");
                courseListFiltered = getCoursList("Ete");
                printListCourses(courseListFiltered);
                return courseListFiltered;
            default:
                System.out.println("erreur: choix invalide, veuillez choisir entre choix existants");
                return null;
        }
    }

    private void printListCourses(ArrayList<Course> list){
        int indice = 0;
        for(Course course : list) {
            indice++;
            System.out.println(indice + ". " + course.getCode() + "     " + course.getName());
        }
    }

    private String getCoursName(ArrayList<Course> list, String codeCours){
        for(Course course : list){
            if(course.getCode().equals(codeCours)){
                return course.getName();
            }
        }
        System.out.println("erreur: code introuvable dans la liste");
        return null;
    }
}
