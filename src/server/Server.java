package server;

import javafx.util.Pair;
import server.models.RegistrationForm;
import server.models.Course;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.*;

import java.util.List;


public class Server {

    public final static String REGISTER_COMMAND = "INSCRIRE";
    public final static String LOAD_COMMAND = "CHARGER";
    private final ServerSocket server;
    private Socket client;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final ArrayList<EventHandler> handlers;

    public Server(int port) throws IOException {
        this.server = new ServerSocket(port, 1);
        this.handlers = new ArrayList<EventHandler>();
        this.addEventHandler(this::handleEvents);
    }

    public void addEventHandler(EventHandler h) {
        this.handlers.add(h);
    }

    private void alertHandlers(String cmd, String arg) {
        for (EventHandler h : this.handlers) {
            h.handle(cmd, arg);
        }
    }

    public void run() {
        while (true) {
            try {
                client = server.accept();
                System.out.println("Connecté au client: " + client);
                objectInputStream = new ObjectInputStream(client.getInputStream());
                objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                listen();
                disconnect();
                System.out.println("Client déconnecté!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void listen() throws IOException, ClassNotFoundException {
        String line;
        if ((line = this.objectInputStream.readObject().toString()) != null) {
            Pair<String, String> parts = processCommandLine(line);
            String cmd = parts.getKey();
            String arg = parts.getValue();
            this.alertHandlers(cmd, arg);
        }
    }

    public Pair<String, String> processCommandLine(String line) {
        String[] parts = line.split(" ");
        String cmd = parts[0];
        String args = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
        return new Pair<>(cmd, args);
    }

    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        client.close();
    }

    public void handleEvents(String cmd, String arg) {
        if (cmd.equals(REGISTER_COMMAND)) {
            handleRegistration();
        } else if (cmd.equals(LOAD_COMMAND)) {
            handleLoadCourses(arg);
        }
    }

    /**
     Lire un fichier texte contenant des informations sur les cours et les transofmer en liste d'objets 'Course'.
     La méthode filtre les cours par la session spécifiée en argument.
     Ensuite, elle renvoie la liste des cours pour une session au client en utilisant l'objet 'objectOutputStream'.
     La méthode gère les exceptions si une erreur se produit lors de la lecture du fichier ou de l'écriture de l'objet dans le flux.
     @param arg la session pour laquelle on veut récupérer la liste des cours
     */
    public void handleLoadCourses(String arg){

        
        BufferedReader reader = null;
        try {

            // /!\ le chemin du fichier cours.txt peut causer une erreur 
            //Lecture du fichier cours.txt
            FileReader cours = new FileReader("cours.txt");
            reader = new BufferedReader(cours);

            //Création de la liste d'objets qui va contenir tous les cours.
            List<Course> allCourses = new ArrayList<>();

            String line = reader.readLine();
            while(line != null){

                //On créé un tableau contenant les éléments de chaque ligne
                String [] elements = line.split("\t");

                String nom = elements[0];
                String code = elements[1];
                String session = elements[2];

                //On créé un cours pour chaque ligne puis on l'ajoute à la liste
                //allCourses
                Course course = new Course(nom, code, session);
                allCourses.add(course);
                
                line = reader.readLine();
            }

            reader.close();

            FileOutputStream fileOutputStream = new FileOutputStream("CoursDeLaSessionDemandee.txt");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            for(Course course : allCourses){
                if (course.getSession().equals(arg)){
                    objectOutputStream.writeObject(course);

                }

            }
            objectOutputStream.close();

        }catch (FileNotFoundException e){
            System.out.println("File not found: " + e.getMessage());

        }
        catch(IOException e){
            System.out.println("Error reading or writing the file: " + e.getMessage());
        }

    }


    /**
     Récupérer l'objet 'RegistrationForm' envoyé par le client en utilisant 'objectInputStream', l'enregistrer dans un fichier texte
     et renvoyer un message de confirmation au client.
     La méthode gére les exceptions si une erreur se produit lors de la lecture de l'objet, l'écriture dans un fichier ou dans le flux de sortie.
     */
    public void handleRegistration() {
        // TODO: implémenter cette méthode
        try {

            ObjectInputStream registration = new ObjectInputStream(objectInputStream);
            RegistrationForm registrationForm = (RegistrationForm) registration.readObject();
            registration.close();

            FileWriter inscription = new FileWriter("inscription.txt");
            BufferedWriter writer = new BufferedWriter(inscription);
            inscription.write(registrationForm.toString()+"\n");

            inscription.close();
            writer.flush();

            System.out.println("inscription enregistree");

        } catch (IOException ioException){
            System.out.println("Erreur à l'ouverture du fichier");
        } catch (ClassNotFoundException classNotFoundException){
            System.out.println("Erreur: ClassNotFoundException");
        }
    }
}