//import processing.core.PApplet;
import processing.core.PImage;

/**
 * A (configurable) picture that can be dragged with the mouse.
 */
public class PictureHandle 
{
  /* "config.txt" Parameter managing the chance to turn the other player card */
  static final String PLAYER_CARD_TURNABLE = "player_card_turnable";  
  static final String CARDS_UNTURNABLE="cards_unturnable";
  static private int id_name = 0; // used as name when not provided in constructor
  static float EASING = 0.20f;

  bengames app = null; //reference to the Processing app engine  
  // Lazy (Processing) class: leave direct access to parameters... Avoids having lot of accessors.
  String name; // Agent name
  String zone;
  String colorName; // card color name
  int num; // card number

  float m_x, m_y; // Position of handle
  int width_img, height_img; // width and height with which to display the pictures
  int m_colorHover;
  int m_colorDrag;

  private boolean m_bIsHovered, m_bDragged, flipImg, relocating;
  private String relocating_zone;
  private float m_clickDX, m_clickDY; // distance from mouse pointer
  private PImage back_img, front_img;
  private boolean player_card_turnable; 
  private boolean cards_unturnable;
  
  /**
   * Simple constructor with hopefully sensible defaults.
   */
  PictureHandle(bengames pa, float x, float y)
  {
    this(pa, x, y, 70, 100, pa.color(255,255,0), pa.color(255,136,0), "card_back_blue.jpeg", "card_back_red.jpeg", String.valueOf(id_name), "dummy_zone", false);
  }

  /**
   * Full constructor.
   *
   * @param x, y location on the screen window
   * @param width_img, height_img actual x, y size of the displayed image (the picture is scaled when displayed)
   */
  PictureHandle(bengames pa, float x, float y, int width_img, int height_img, 
  int colorHover, int colorDrag, String back_image_file, String front_image_file, String name, String zoneName, boolean covered
    )
  {
    //super(x,y);
    this.app = pa;
    this.name = name;
    zone = zoneName;
    colorName = name.substring(2); // infer from agent name
    num = Integer.parseInt(name.substring(0, 1)); // infer from agent name
    m_x = x; 
    m_y = y; // current position on screen
    this.width_img = width_img; // width on screen
    this.height_img = height_img; // height on screen
    m_colorHover = colorHover; // color when hovering
    m_colorDrag = colorDrag; // color when dragging
    back_img = app.loadImage(back_image_file); 
    front_img = app.loadImage(front_image_file);
    
    String parValue = app.config.getParamValue(PLAYER_CARD_TURNABLE);
    if (parValue.equals("1"))
      player_card_turnable=true;
    else player_card_turnable=false;

    parValue = app.config.getParamValue(CARDS_UNTURNABLE);
    if (parValue.equals("1"))
      cards_unturnable=true;
    else cards_unturnable=false;

    if (covered) flipImg = true; // card is covered alias flipped: the back is shown
    else flipImg = false;  
    relocating = false; // when is changing zone
    relocating_zone=""; // the zone where to move when relocating
    id_name++;
  }

  /**
   * Updates the state of the handle depending on the mouse position.
   * Essentially, decides whether the dragging is on or off.
   *
   * @param bAlreadyDragging  if true, a dragging is already in effect
   */
  void update(boolean bAlreadyDragging)
  { 
    /* GAME CHECK: flip the card according to turn, player and zone: */
    if (zone.equals("Color" ) && app.cplayer.equals("nk" ) )
      flipImg=true;
    else if (zone.equals("Color" ) && app.cplayer.equals("ck" ))
      flipImg=false;
    else if (zone.equals("Number" ) && app.cplayer.equals("ck" ))
      flipImg=true;
    else if (zone.equals("Number" ) && app.cplayer.equals("nk" ))
      flipImg=false;
    else if (zone.equals("DownC") || (zone.equals("DownN")) ) 
      flipImg = true;
    else if (zone.equals("Up" ))
      flipImg=false;
    else if (zone.equals("Target") )
      flipImg=false;

    if (relocating)
      relocate();

    else {       
      // Check if mouse is over the handle
      if (app.mouseX > m_x && app.mouseX < m_x + width_img &&
        app.mouseY > m_y && app.mouseY < m_y + height_img)
        m_bIsHovered = true;
      else
        m_bIsHovered= false; 

      // If we are not already dragging and left mouse is pressed over the handle
      if (!bAlreadyDragging && app.mousePressed && app.mouseButton == app.LEFT && m_bIsHovered) {
        // GAME CHECK:
        /* Only the card in Color or Number zones can be dragged wether 
         is playing the colorkeeper or numberkeeper respectively */
        // println(" zone: "+zone);
        if (!zone.equals("Color") && app.cplayer.equals("ck")) 
          return;
        if (!zone.equals("Number") && app.cplayer.equals("nk"))
          return;

        // We record the state
        m_bDragged = true;
        // And memorize the offset of the mouse position from the center of the handle
        m_clickDX = app.mouseX - m_x;
        m_clickDY = app.mouseY - m_y;
      }

      if (app.mousePressed && app.mouseButton == app.RIGHT && m_bIsHovered) {
        if ( (zone.equals("Color") && app.cplayer.equals("nk") && player_card_turnable) ||
          (zone.equals("Number") && app.cplayer.equals("ck") && player_card_turnable) )
          flipImg = !flipImg;
        else if (!cards_unturnable)
          flipImg = !flipImg;
      }

      // If mouse isn't pressed
      if (!app.mousePressed ) {
        // Any possible dragging is stopped
        m_bDragged = false;

        boolean overAZone = false;
        int my_zone_index = app.cards_zones_names.indexOf(this.zone );
        int next_zone_index = -1;       

        /* we suppose a card can only cover a zone at a time */
        for (int i = 0; i < app.HOME_CARDS_COORDINATES.length/2; i++) {
          if (i == my_zone_index) continue;

          if (m_x > app.HOME_CARDS_COORDINATES[i+i] && m_x < app.HOME_CARDS_COORDINATES[i+i] + width_img && 
            m_y > app.HOME_CARDS_COORDINATES[i+i+1] && m_y < app.HOME_CARDS_COORDINATES[i+i+1] + height_img) {
            /* GAME CHECK: if it is an legal move */
            if (app.allowed_moving_zones.contains(app.cards_zones_names.get(i))) { 
              if ( app.cards_zones_names.get(i).equals("Target" )) { 
                if ( ( app.cplayer.equals("ck" ) && app.hlist.get(app.zones_cards.get(app.cards_zones_names.get(i))).colorName.equals(this.colorName) ) ||
                  ( ( app.cplayer.equals("nk" ) && app.hlist.get(app.zones_cards.get(app.cards_zones_names.get(i))).num == this.num )  ) 
                  )
                { 
                  overAZone = true;
                  next_zone_index = i;
                  // println("OVER ZONE: "+next_zone_index);
                  break;
                }
              } else {
                overAZone = true;
                next_zone_index = i;
                // println("OVER ZONE: "+next_zone_index);
                break;
              }
            }
          }
        }

        if (overAZone) {
          relocating_zone = app.cards_zones_names.get(next_zone_index); // gets zone name from index
          app.handHistory.append(app.ZONES_MOVES.get(relocating_zone) );
          //println("Player " + app.cplayer + " moved: "+app.ZONES_MOVES.get(relocating_zone ) );
          
          relocate(); // relocate myself
          PictureHandle neighbor = app.hlist.get(app.zones_cards.get(relocating_zone )); // the card (by name) currently in the zone where we are moving 
          neighbor.relocate(this.zone); // relocate the other onto my zone

          /* GAME CHECK: Player turn change and if the hand is ended: */
          app.moves_counter++;
          if (relocating_zone.equals("Target" ) && name.equals(app.current_target)) {
            app.runs_counter++;
            app.total_moves += app.moves_counter;
            app.moves_counter = 0;
            if (app.runs_counter > app.MAX_RUNS) { // Game ends
              //println("Game session ended.\n");
              // println();
              /* reset: */
              app.total_moves=0;
              app.runs_counter=0;
            }

            //println("Hands successfull:\n"+app.handHistory.toString()+"\nGenerating a new hand.");
            app.handHistory.setLength(0 ); // resets
            app.replaceHandles(); // generate a new (random) run
          } else if (app.cplayer.equals("ck" )) app.cplayer="nk";
          else app.cplayer="ck";
        } else {
          // if it is not in its place, move to its previous position:
          relocate(zone);
        }
      }
    }
  }

  boolean isDragged()
  {
    return m_bDragged;
  }

  boolean isHovered() {
    return m_bIsHovered;
  }

  void relocate() {
    relocating = true;
    int index = app.cards_zones_names.indexOf(relocating_zone);
    float d = move(app.HOME_CARDS_COORDINATES[index+index], app.HOME_CARDS_COORDINATES[index+index+1]);
    if (d < 1) {
      this.zone = relocating_zone;
      this.relocating = false;
      relocating_zone = "";
      app.zones_cards.put(this.zone, name);
    }
  }

  void relocate(String zone_to) {
    relocating_zone = zone_to;
    relocate();
  }

  /**
   * If the handle is dragged, the new position is computed with mouse position,
   * taking in account the offset of mouse with center of handle.
   */
  void move()
  {
    if (m_bDragged)
    {
      m_x = app.mouseX - m_clickDX;
      m_y = app.mouseY - m_clickDY;
    }
  }

  /**
   * Move to a position in smooth steps.
   */
  float move(int x, int y) {
    float vx = (x - m_x) * EASING;
    float vy = (y - m_y) * EASING;

    m_x += vx;
    m_y += vy;

    // With the above adjustments  x and y should never
    // be equal to x, y (i.e., Zeno's paradox)
    // so you just check once they're near enough...
    float d = app.dist(m_x, m_y, x, y);
    if (d < 1) {
      // Set a new target
      // In theory you could set up an array of targets for an icon to follow
      // Might be a good option if your icons always follow the same path
      m_x = x;
      m_y = y;
    }   
    return d;
  }


  /**
   * Just draw the handle at current posiiton, with color depending if it is dragged or not.
   */
  void display()
  {
    if (m_bDragged)
    {
      app.stroke(m_colorDrag);
      app.rect(m_x-3, m_y-3, width_img+3, height_img+3, 3);
    } else if (m_bIsHovered)
    {
      app.stroke(m_colorHover);
      app.rect(m_x-3, m_y-3, width_img+3, height_img+3, 3);
    }
    if (flipImg)
      app.image(back_img, m_x, m_y, width_img, height_img);
    else
      app.image(front_img, m_x, m_y, width_img, height_img);
  }

  /** Implements the local agent (active) behavior.
   */
  void behavior()
  {
  }
}
