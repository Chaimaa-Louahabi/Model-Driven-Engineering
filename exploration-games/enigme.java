import java.util.ArrayList;
import java.util.Random;

import java.util.Scanner;

public class enigme extends Thread {
	
	private class Game {
		Player player;
		ArrayList<Place> listePlace;
		ArrayList<Chemin> listeChemin;
		Place placeActuelle;
		public Game(Player player, ArrayList<Place> listePlace, ArrayList<Chemin> listeChemin, Place placeActuelle) {
			super();
			this.player = player;
			this.listePlace = listePlace;
			this.listeChemin = listeChemin;
			this.placeActuelle = placeActuelle;
		}
	}
	
	protected class Player {
		String name;
		int nombreObjetsMax;
		int nombreObjets;
		public Player(String name, int nombreObjets, int nombreObjetsMax) {
			super();
			this.name = name;
			this.nombreObjetsMax = nombreObjetsMax;
			this.nombreObjets = nombreObjets;
		}
	}
	
	protected class Personne {
		String name;
		public Personne (String name) {
			this.name = name;
		}
	}
	
	protected class Place {
		String name;
		String type;
		int allocations;
		ArrayList<Chemin> listeChemins;
		ArrayList<Personne> listePersonnes;
		ArrayList<Knowledge> listeKnowledge;
		public Place(String name, String type, int allocations, ArrayList<Personne> listePersonnes,
				ArrayList<Knowledge> listeKnowledge) {
			super();
			this.name = name;
			this.type = type;
			this.allocations = allocations;
			this.listePersonnes = listePersonnes;
			this.listeKnowledge = listeKnowledge;
		}
		
	}
	
	protected class Chemin {
		Place from;
		Place to;
		Boolean visible;
		public Chemin(Place from, Place to, Boolean visible) {
			this.from = from;
			this.to = to;
			this.visible = visible;
		}
	}
	
	protected class Knowledge {
		String name;
		Boolean state;
		public Knowledge(String name, Boolean state) {
			super();
			this.name = name;
			this.state = state;
		}
	}
	
	protected class Objet {
		String name;
		double size;
		Boolean state;
		public Objet(String name, double size, Boolean state) {
			super();
			this.name = name;
			this.size = size;
			this.state = state;
		}
	}
	

	protected void allocate(Player player, int nbAllocated) {
		player.nombreObjets += nbAllocated;
		if (player.nombreObjets > player.nombreObjetsMax) {
			player.nombreObjets = player.nombreObjetsMax;
		}
	}
	
	protected class Reponse {
		String reponse;
		boolean valide;
		public Reponse(String reponse, boolean valide) {
			super();
			this.reponse = reponse;
			this.valide = valide;
		}
	}
	
	protected class QuestionReponse {
		String question;
		ArrayList<Reponse> listeReponse;
		public QuestionReponse(String question, String rep1, boolean rep1valide, String rep2, boolean rep2valide,
				String rep3, boolean rep3valide, String rep4, boolean rep4valide) {
			this.question = question;
			this.listeReponse = new ArrayList<Reponse>();
			this.listeReponse.add(new Reponse(rep1, rep1valide));
			this.listeReponse.add(new Reponse(rep2, rep2valide));
			this.listeReponse.add(new Reponse(rep3, rep3valide));
			this.listeReponse.add(new Reponse(rep4, rep4valide));
		}
		
		protected boolean poserQuestion() {
			boolean reponseJuste = false;
			// Affichage de la question et des réponses
			System.out.println("Le sphynx vous pose la question suivante :");
			System.out.println(this.question);
			for (Reponse reponse : this.listeReponse) {
				System.out.println(reponse.reponse);
			}
			
			// On récolte la réponse
			Scanner input = new Scanner(System.in);
			System.out.print("Quelle est votre réponse ? : ");
			int reponse = input.nextInt() -1;
			
			try {
				reponseJuste = this.listeReponse.get(reponse).valide;
			}
			catch (Exception e) {
				System.out.println("Vous venez de proposer une réponse qui n'est pas proposée, dommage");
			}
			
			return reponseJuste;
		}
	}
	
	
	public Game initPartieEnigme() {
		// Création du joueur
		String nomJoueur = "anonyme";
		int nombreObjets = 0;
		int nombreObjetsMax = 3;
		Player player = new Player(nomJoueur, nombreObjets, nombreObjetsMax);
		
		// Création de la place Success
		String nomPlace = "Success";
		String type = "end";
		int allocations = 0;
		ArrayList<Personne> listePersonne = new ArrayList<Personne>(0);;
		ArrayList<Knowledge> listeKnowledge = new ArrayList<Knowledge>(0);
		Place placeSuccess = new Place(nomPlace, type, allocations, listePersonne, listeKnowledge);
		
		// Création de la place Echec
		nomPlace = "Echec";
		Place placeEchec = new Place(nomPlace, type, allocations, listePersonne, listeKnowledge);
		
		// Création de la place Enigme
		nomPlace = "Enigme";
		type = "begin";
		allocations = 3;
		Personne sphynx = new Personne("Combien de gouttes d’eau peut-on mettre dans un verre vide ?");
		listePersonne.add(sphynx);
		Knowledge knowledgeReussite = new Knowledge("Reussite", true);
		listeKnowledge.add(knowledgeReussite);
		Place placeEnigme = new Place(nomPlace, type, allocations, listePersonne, listeKnowledge);
		
		
		// Création des chemins
		Chemin enigme2success = new Chemin(placeEnigme, placeSuccess, false);
		Chemin enigme2echec = new Chemin(placeEnigme, placeEchec, false);
		
		
		// Création de la partie
		ArrayList<Chemin> listeChemin = new ArrayList<Chemin>(0);
		listeChemin.add(enigme2success);
		listeChemin.add(enigme2echec);
		ArrayList<Place> listePlace = new ArrayList<Place>(0);
		listePlace.add(placeEnigme);
		listePlace.add(placeSuccess);
		listePlace.add(placeEchec);
		
		Place placeActuelle = placeEnigme;
		
		// Allocation des 3 tentatives au joueur
		allocate(player, 3);
		
		Game game = new Game(player, listePlace, listeChemin, placeActuelle);
		return game;
	}
	
	public void gererPartieEnigme() {
		Game game = initPartieEnigme();
		System.out.println("=================================================");
		System.out.println("Bienvenue dans le jeu des enigmes, vous êtes un joueur anonyme qui doit répondre aux"
				+ "questions que le Sphynx va vous poser");
		System.out.println("Vous disposez de 3 essais. Une seule bonne réponse parmi celles proposées et vous gagnez");
		System.out.println("Pour répondre, tapez le numéro de la réponse et appuyez sur entrée.");
		System.out.println("");
		ArrayList<QuestionReponse> listeQuestionReponse = new ArrayList<QuestionReponse>();
		listeQuestionReponse.add(new QuestionReponse(
				"Combien de gouttes d'eau peut-on mettre dans un verre vide ?",
				"1 : Une", true, 
				"2 : Deux", false, 
				"3 : Trois", false, 
				"4 : Quatre", false));
		Random random = new Random();
		while (game.placeActuelle.type != "end") {
			int indiceQuestion = random.nextInt(listeQuestionReponse.size());
			QuestionReponse questionReponse = listeQuestionReponse.get(indiceQuestion);
			Boolean reponseCorrecte = questionReponse.poserQuestion();
			if (reponseCorrecte) {
				game.placeActuelle = game.listePlace.get(1);
			} else {
				if (game.player.nombreObjets > 1) {
					game.player.nombreObjets -= 1;
					if (game.player.nombreObjets == 1) {
						System.out.println("Perdu ! Il vous reste 1 tentative");
					} else {
						System.out.println("Perdu ! Il vous reste " + game.player.nombreObjets + " tentatives");
					}
				} else {
					game.placeActuelle = game.listePlace.get(2);
				}
			}
			System.out.println("");
			System.out.println("");
		}
		if (game.placeActuelle == game.listePlace.get(1)) {
			// Cas où le joueur réussit
			System.out.println("Bravo ! Vous avez gagné le jeu");
		} else {
			// Cas où on a tort
			System.out.println("Dommage, vous avez perdu. La prochaine fois sera peut-être meilleure");
		}
		
	}
	
	public static void main(String[] args) {
		enigme e = new enigme();
		e.gererPartieEnigme();
	}

}
	;