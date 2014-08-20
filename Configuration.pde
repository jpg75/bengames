
class Configuration {

  static final String DEFAULT_RESOURCE = "config.txt";

  String resource;
  private String[] lines;
  private HashMap<String, String> items;

  Configuration() {
    this(DEFAULT_RESOURCE);
  }

  Configuration(String resource) {
    this.resource = resource;
    lines = loadStrings(this.resource );
    items= new HashMap<String, String>();
    this.initialize();
  }

  /** Build the hash mapping parameter -> value
   */
  void initialize() {
    for (String line : lines) {
      if (! isComment(line )) { // if not a comment:
        String results[] = line.split("=");
        if (results.length > 1 && results.length ==2) 
          items.put(results[0], results[1]);
      }
    }
  }

  String[] loadFile(String filename) {
    return loadStrings(filename);  
  }
  
  
  boolean exist(String param) {
    return items.containsKey(param);  
  }
  
  String getParamValue(String param, String defaultValue) {
    String result= getParamValue(param );
    if (result== null) result=defaultValue;
    return result;
  }
  
  /** Retrieve the param corresponding value.
   */
  String getParamValue(String param) {
    return items.get(param );
  }

  /** Retrieve an array of strings representing all the available parameters in the current config file.
   */
  Set<String> params() {
    return items.keySet();
  }

  /* Remove comment lines from the array.
   Purging make sure that the line array is not null, but empty in the worst case.
   */
  void purge() {
    ArrayList<String> sl = new ArrayList<String>();

    if (lines!= null) {
      for (int i= 0; i< lines.length; i++) {
        if (! isComment(lines[i]))
          sl.add(lines[i]);
      }
    }

    lines = (String[]) sl.toArray();
  }

  /**Check if a string is a comment or not.
  */ 
  boolean isComment(String l) {
    return (l.startsWith("#" ) || l.startsWith("//" ));
  }
}
