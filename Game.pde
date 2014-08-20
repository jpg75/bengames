/**
* The basic class Game must be subclassed and implemented by a specialized class.
* The idea is to have a place where the initialization of the game world takes place
* and shared state of the game is managed.
* However, we suppose that 'agents' are actually playing the game.
* The agents can be driven by a synthetic behavior or can be the avatar of a human player.
*/
public abstract class Game {
  public String name;
  
  abstract void setup();
  
}


public class TTT extends Game {
  final boolean[] TTT_COVERED_CARDS_CK = new boolean[] {false, false, true, true, false, true};
  final boolean[] TTT_COVERED_CARDS_NK = new boolean[] {true, false, false, true, false, true};
  final String PLAYER_FIRST = "ck"; // 'ck' or 'nk' are valid choices
  final String[] MOVES = new String[] {"u", "c", "n", "t", "p"};
  final int MOVE_COST = 1;
  final int MAX_RUNS = 40;
  
  String current_turn_player; // the actual turn player
  String current_target = "2h"; // card name of the current target card. It may change during game!

  int moves_counter=0; // number of moves
  int runs_counter=0; //number of runs
  int total_moves = 0; // total number of moves
  
  public void setup(){
    current_turn_player = PLAYER_FIRST;
    ;
  }
  
}
