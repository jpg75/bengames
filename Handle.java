import processing.core.PApplet;

/**
 * A rectangle that can be dragged with the mouse.
 */
public class Handle
{
  // Lazy (Processing) class: leave direct access to parameters... Avoids having lot of accessors.
  float m_x, m_y; // Position of handle
  int m_size; // Diameter of handle
  int m_lineWidth;
  int m_colorLine;
  int m_colorFill;
  int m_colorHover;
  int m_colorDrag;
  private PApplet app = null; //reference to the Processing app engine
  private boolean m_bIsHovered, m_bDragged;
  private float m_clickDX, m_clickDY;

  /**
   * Simple constructor with hopefully sensible defaults.
   */
  Handle(PApplet pa, float x, float y)
  { 
    this(pa, x, y, 5, 1, pa.color(0,0,0), pa.color(255,255,255), pa.color(255,255,0), pa.color(255,136,00));
  }

  /**
   * Full constructor.
   */
  Handle(PApplet pa, float x, float y, int size, int lineWidth,
      int colorLine, int colorFill, int colorHover, int colorDrag
  )
  {
    this.app=pa;
    m_x = x; m_y = y;
    m_size = size;
    m_lineWidth = lineWidth;
    m_colorLine = colorLine;
    m_colorFill = colorFill;
    m_colorHover = colorHover;
    m_colorDrag = colorDrag;
  }

  /**
   * Updates the state of the handle depending on the mouse position.
   * Essentially, decides whether the dragging is on or off.
   *
   * @param bAlreadyDragging  if true, a dragging is already in effect
   */
  void update(boolean bAlreadyDragging)
  {
    // Check if mouse is over the handle
    m_bIsHovered = app.dist(app.mouseX, app.mouseY, m_x, m_y) <= m_size / 2;
    // If we are not already dragging and left mouse is pressed over the handle
    if (!bAlreadyDragging && app.mousePressed && app.mouseButton == app.LEFT && m_bIsHovered)
    {
      // We record the state
      m_bDragged = true;
      // And memorize the offset of the mouse position from the center of the handle
      m_clickDX = app.mouseX - m_x;
      m_clickDY = app.mouseY - m_y;
    }
    // If mouse isn't pressed
    if (!app.mousePressed)
    {
      // Any possible dragging is stopped
      m_bDragged = false;
    }
  }

  boolean isDragged()
  {
    return m_bDragged;
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
    * Just draw the handle at current posiiton, with color depending if it is dragged or not.
    */
  void display()
  {
    app.strokeWeight(m_lineWidth);
    app.stroke(m_colorLine);
    if (m_bDragged)
    {
      app.fill(m_colorDrag);
    }
    else if (m_bIsHovered)
    {
      app.fill(m_colorHover);
    }
    else
    {
      app.fill(m_colorFill);
    }

    app.ellipse(m_x, m_y, m_size, m_size);
  }
}
