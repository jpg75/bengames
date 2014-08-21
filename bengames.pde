import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

/**
 Handles demo: A first implementaion on the Target the Two (TTT) card game.
 */
public class bengames extends PApplet {
	
//PApplet benSim = this;

static StringBuilder handHistory = new StringBuilder(); // hystory of players moves

static final String SHOE_FILE_CFG="shoe_file";
static final String FORMAT_CFG=	"format"; // shoe file and output format, "linear" is default

final int HANDLE_NB = 6;
final int CARD_WIDTH = 70;
final int CARD_HEIGHT = 100;
final int CARD_GAP = 50;

final boolean[] TTT_COVERED_CARDS = new boolean[] {
  false, false, false, true, false, true
};

final String[] LINEAR_CARD_LAYOUT= new String[] {
  "DownC", "Color", "Up", "Target", "DownN", "Number"
};

final String[] CIRCULAR_CARD_LAYOUT= new String[] {  
  "Number", "DownN", "Up", "DownC", "Color", "Target"
};

// holds where the cards has to go
final int[] HOME_CARDS_COORDINATES = new int[] {
  CARD_GAP, CARD_GAP, (CARD_GAP*2)+CARD_WIDTH, CARD_GAP, (CARD_GAP*3)+(CARD_WIDTH*2), CARD_GAP, 
  CARD_GAP, (CARD_GAP*2)+CARD_HEIGHT, (CARD_GAP*2)+CARD_WIDTH, (CARD_GAP*2)+CARD_HEIGHT, 
  (CARD_GAP*3)+(CARD_WIDTH*2), (CARD_GAP*2)+CARD_HEIGHT
};

String[] cards_files = new String[] {
  "h2red.jpeg", "h3red.jpeg", "h4red.jpeg", "c2black.jpeg", "c3black.jpeg", "c4black.jpeg"
};

String[] cards_names = new String[] {
  "2h", "3h", "4h", "2c", "3c", "4c"
}; 

/* zones here follow the order in HOME_CARDS_COORDINATES array */
ArrayList<String> cards_zones_names = new ArrayList<String>() {
  { 
    add("Color"); 
    add( "Up"); 
    add("Number"); 
    add("DownC"); 
    add("Target"); 
    add("DownN");
  }
};

/* Allowed zones in which players can exchange/move cards. 'Target' zone in subjected to 
 extra restrictions.*/
final ArrayList<String> allowed_moving_zones = new ArrayList<String>() {
  { 
    add("Up") ; 
    add("Target"); 
    add("DownC"); 
    add("DownN");
  }
};

/* map zones -> moves */
final HashMap<String, String> ZONES_MOVES = new HashMap<String, String>() {
  {
    put("Up", "u");
    put("DownC", "c");
    put("DownN", "n");
    put("Target", "t");
    put("Pass", "p");
  }
};

// map card name -> card picture file
final HashMap<String, String> cards_names_files = new HashMap<String, String>() {
  {
    put("2h", "h2red.jpeg");
    put("3h", "h3red.jpeg");
    put("4h", "h4red.jpeg");
    put("2c", "c2black.jpeg");
    put("3c", "c3black.jpeg");
    put("4c", "c4black.jpeg");
  }
};

final int MAX_RUNS = 40;
HashMap<String, String> zones_cards = new HashMap<String, String>();
String cplayer= "ck"; // colorkeeper always moves first, then numberkeeper
String current_target = "2h"; // card name of the current target card. It may change during game!
String shoe_filename; 
String[] shoe_file_lines = null;
String format= "linear"; // "linear" or "circular" 
int moves_counter=0; // number of moves
int total_moves = 0; // total number of moves
int runs_counter = 1; //number of runs

public Configuration config;
HandleList hlist = new HandleList(false);

PFont font;

public void setup()
{
  size(640, 480);
  smooth();
  font = createFont("Arial", 16, true);
  config = new Configuration(this);
  shoe_filename = config.getParamValue(SHOE_FILE_CFG);
  if (shoe_filename != null){
    println("Opening file: " + shoe_filename);
    shoe_file_lines = config.loadFile(shoe_filename);
  }
  
  if (config.exist(FORMAT_CFG))
    format = config.getParamValue(FORMAT_CFG);
    
  println(cards_names_files );

  // Create random handles
  for (int i = 0; i < HANDLE_NB; i++)
  {
    hlist.add(new PictureHandle(this, HOME_CARDS_COORDINATES[i+i], HOME_CARDS_COORDINATES[i+i+1], 70, 100, 
    #FFFF00, #FF8800, "card_back_blue.jpeg", cards_files[i], cards_names[i], cards_zones_names.get(i), TTT_COVERED_CARDS[i]));
    zones_cards.put(cards_zones_names.get(i), cards_names[i]);
  }
}

public void draw()
{
  background(0, 102, 51);

  // Writes the labels on top of the card zones
  textFont(font, 16);
  fill(0);
  for (int i = 0; i < HOME_CARDS_COORDINATES.length/2; i++) {
    text(cards_zones_names.get(i), HOME_CARDS_COORDINATES[i+i], HOME_CARDS_COORDINATES[i+i+1] -8 );
  }

  // writes the turn owner:
  text("Current player turn: "+ cplayer, 50, 350 );
  text("Run: "+ runs_counter + ", Number of moves: "+moves_counter, 50, 370);
  text("Total moves: "+total_moves, 50, 400);

  /* draw a green rounded box around the palywr card */
  noFill();
  stroke(#7BFA38); // green
  strokeWeight(2);
  if (cplayer.equals("ck" ))
    rect(CARD_GAP-5, CARD_GAP-5, (70)+5*2, (100)+5*2, 5);
  else
    rect((CARD_GAP*3)+(CARD_WIDTH*2)-5, CARD_GAP-5, (70)+5*2, (100)+5*2, 5);

  /* draw the current target card, default "2h" */
  float xstart=(CARD_GAP*3)+(CARD_WIDTH*2)+CARD_WIDTH+50;
  float ystart=CARD_GAP+50;
  text("Current Target:", xstart, ystart-15);
  image(loadImage((String)cards_names_files.get(current_target)), xstart, ystart, 70*1.4, 100*1.4);
  stroke(#FF1500);
  strokeWeight(4 );
  rect(xstart-10, ystart-10, (70*1.4)+10*2, (100*1.4)+10*2, 7);
  stroke(0 );
  strokeWeight(1 );
  line(xstart-25, 0, xstart-25, height);

  hlist.update();
}

public void keyPressed() {
  if (key == 'n') {
    hlist.reset();
    replaceHandles();
  } else { // pass the turn
    /* GAME CHECK: */
    handHistory.append("p");
    println("Player "+cplayer+ " moved: "+ZONES_MOVES.get("Pass"));
    moves_counter++;

    if (cplayer.equals("ck"))
      cplayer="nk";
    else cplayer="ck";
  }
}

/** Generate a new set of handles (cards) in random positions or using the shoe file if any.
 */
public void replaceHandles() {
  if (shoe_filename == null) {
    shuffleArray(cards_names);
    println(cards_names);
    /* Must check that card '2h' in not put in 'Target' zone or the game never ends! */
    if (cards_names[4].equals("2h")) {
      // swap in last position:
      String temp = cards_names[cards_names.length-1];
      cards_names[4]=temp;
      cards_names[cards_names.length-1]= "2h";
    }

    for (int i=0; i< cards_names.length; i++) {
      hlist.add(new PictureHandle(this, HOME_CARDS_COORDINATES[i+i], HOME_CARDS_COORDINATES[i+i+1], 70, 100, 
      #FFFF00, #FF8800, "card_back_blue.jpeg", cards_names_files.get(cards_names[i]), cards_names[i], cards_zones_names.get(i), TTT_COVERED_CARDS[i]));
      zones_cards.put(cards_zones_names.get(i), cards_names[i] );
    }
    cplayer="ck";
  }
  else{
    String[] items = parseShoeLine(shoe_file_lines[runs_counter]);
    //println(items);
    //sprintln(items.length);
    if (items.length != 8) {
      println("Wrong shoe file format at line: "+runs_counter);
      exit();
    }
    else{
      println(items);
      cards_names = Arrays.copyOfRange(items, 0, 6);
      println(cards_names);
      if (format.equals("circular")){
        for (int i = 0 ; i< 6 ; i++){
          hlist.add(new PictureHandle(this, HOME_CARDS_COORDINATES[i+i], HOME_CARDS_COORDINATES[i+i+1], 70, 100, 
          #FFFF00, #FF8800, "card_back_blue.jpeg", cards_names_files.get(cards_names[i]), cards_names[i], CIRCULAR_CARD_LAYOUT[i], TTT_COVERED_CARDS[i]));
          zones_cards.put(CIRCULAR_CARD_LAYOUT[i], cards_names[i] );
        }
      }
      else{
        for (int i = 0 ; i< 6 ; i++) {
          hlist.add(new PictureHandle(this, HOME_CARDS_COORDINATES[i+i], HOME_CARDS_COORDINATES[i+i+1], 70, 100, 
          #FFFF00, #FF8800, "card_back_blue.jpeg", cards_names_files.get(cards_names[i]), cards_names[i], LINEAR_CARD_LAYOUT[i], TTT_COVERED_CARDS[i]));
          zones_cards.put(LINEAR_CARD_LAYOUT[i], cards_names[i] );
        }
      }
      current_target = items[6];
      cplayer = items[7]; 
    }  
  }
}

public String[] parseShoeLine(String line) {
  line = line.trim();
  line = line.toLowerCase();
  return line.split("\\s+");
}

public void shuffleArray(Object[] ar)
{
  for (int i = ar.length - 1; i > 0; i--)
  {
    int index = (int)random(i+1);
    // Simple swap
    Object a = ar[index];
    ar[index] = ar[i];
    ar[i] = a;
  }
}

}
