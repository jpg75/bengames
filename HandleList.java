import java.util.HashMap;

/**
 * A list of handles.
 */
public class HandleList
{
  
  private HashMap<String, PictureHandle> handles = new HashMap<String, PictureHandle>();
  private boolean m_bDragging;
  private boolean m_bGroupDragging; // True if you want to be able to drag several handles at once (if they are on the same position)
  
  HandleList() // Empty constructor
  {
  }

  HandleList(boolean bGroupDragging)
  {
    m_bGroupDragging = bGroupDragging;
  }

  /** Adds a Handle object to the list.
  */
  void add(PictureHandle h)
  {
    handles.put(h.name, h);
  }

  PictureHandle get(String name){
    return handles.get(name );
  }
  
  /** Removes all the items in the list.
  */
  void reset(){
    handles.clear();
  }
  
  /** List main cycle: manages each Handle element.
  */
  void update()
  {
    // We suppose we are not dragging by default
    boolean bDragging = false;
    // Check each handle
    for (PictureHandle h : handles.values()) // forall objects:
    {
      // Check if the user tries to drag it
      h.update(m_bDragging);
      // Ah, this one is indeed dragged!
      if (h.isDragged())
      {
        // We will remember a dragging is being done
        bDragging = true;
        if (!m_bGroupDragging)
        {
          m_bDragging = true; // Notify immediately we are dragging something
        }
        // And we move it to the mouse position
        h.move();
      }
     
      // In all cases, we redraw the handle
      h.display();
      
      // Call the agent local behavior. It is called at the end of the process
      // bacause it may react to the GUI action
      h.behavior();
    }
    // If no dragging is found, we reset the state
    m_bDragging = bDragging;
  }
  
  /** Report how many hovered items are there.
  */
  int countHovered(){
    int result=0;
    for (PictureHandle ph : handles.values()) {
      if (ph.isHovered())
        result++;
    }
    return result;
  }
  
  HashMap<String, PictureHandle> hoveredHandles(){
    HashMap<String, PictureHandle> result = new HashMap<String, PictureHandle>();
    for (PictureHandle ph : handles.values()) {
      if (ph.isHovered())
        result.put(ph.name, ph);
    }
    
    return result;  
  }
  
}
