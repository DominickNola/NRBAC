import java.io.*;
import java.util.*;
import java.util.Scanner;

class Node {
    // List of ascendants.
    ArrayList<Node> ascendant = new ArrayList<>();
    Node descendant;

    String roleName;
    String descendantName = null;
}

class NRBAC {

    //create a HashMap to store role hierarchy.
    static HashMap<String, Node> roleHierarchy = new HashMap<>();
    static HashMap<String, Node> addedRoles = new HashMap<>();
    static Map<String, List<String>> permissionHash = new HashMap<>();
    static ArrayList<String> descendantRole = new ArrayList<>();
    static String[] allRoles;
    static ArrayList<String> runningRoles = new ArrayList<>();
    static ArrayList<String> allObjects = new ArrayList<>();
    static ArrayList<String> listRoles = new ArrayList<>();
    static ArrayList<String> grantRoles = new ArrayList<>();
    static ArrayList<String> grantAccessRights = new ArrayList<>();
    static ArrayList<String> grantObjects = new ArrayList<>();
    static String twoD[][];
    static HashMap<Integer, List<String>> roleSets = new HashMap<>();
    static HashMap<String, List<String>> userRoles = new HashMap<>();

    public static void main(String[] args) throws Exception {

        //read roles from roleHierarchy.txt
        File readRoleHierarchy = new File("roleHierarchy.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(readRoleHierarchy));
        // String to store roles from txt file.
        String rolesString;

        int readLines = 1;
        while ((rolesString = bufferedReader.readLine()) != null) {

            allRoles = rolesString.split("\\s+");
            //create a new role from the Role class below
            Node newRole = new Node();
            // roleName stored.
            newRole.roleName = allRoles[0];
            if (addedRoles.containsKey(newRole.roleName) && (!addedRoles.get(allRoles[0]).descendantName.equals(allRoles[1]))) {

                System.out.print("Invalid line is found in roleHierarchy.txt: line <" + readLines + ", " + allRoles[0] +
                        " " + allRoles[1] + ">. ");
                promptEnterKey();
            }

            readLines++;
            // descendantName stored.
            newRole.descendantName = allRoles[1];
            // Put elements in the map. (R2's name, R2's role class)
            addedRoles.put(newRole.roleName, newRole);
            runningRoles.add(allRoles[0]);
        }

        bufferedReader.close();
        roleHierarchy.putAll(addedRoles);

        // Get a set of the entries.
        Set<HashMap.Entry<String, Node>> setMap = addedRoles.entrySet();

        // Display the set.
        for(HashMap.Entry<String, Node> printSet : setMap) {

            // create a String of setDescendants and get values from the set of entries.
            String setDescendants = printSet.getValue().descendantName;
            // Search roleHierarchy for descendants Key value, create if it doesn't exist.
            if (!roleHierarchy.containsKey(setDescendants)) {

                Node newRole = new Node();
                newRole.roleName = setDescendants;
                roleHierarchy.put(setDescendants, newRole);
                descendantRole.add(setDescendants);
            }

            roleHierarchy.get(setDescendants).ascendant.add(printSet.getValue());
            printSet.getValue().descendant = roleHierarchy.get(setDescendants);
        }

        for (int i = 0; i < descendantRole.size(); i++) {

            System.out.println();
            printTree(descendantRole.get(i), 0);
        }
        System.out.println();

        //read roles from resourceObjects.txt
        File readResourceObjects = new File("resourceObjects.txt");
        BufferedReader brResourceObjects = new BufferedReader(new FileReader(readResourceObjects));
        // String to store objects from .txt file.
        String objectsString;
        String[] objectsArray;

        System.out.print("\t");
        for(String element3: descendantRole) {

            allObjects.add(element3);
            listRoles.add(element3);
        }
        for(String element2: runningRoles) {

            allObjects.add(element2);
            listRoles.add(element2);
        }
        while((objectsString = brResourceObjects.readLine()) != null) {

            objectsArray = objectsString.split("\\s+");
            for(String element: objectsArray) {

                allObjects.add(element);
            }
        }

        // Print top row.
        for(String element4: allObjects) {

            System.out.print(element4 + "\t\t");
        }
        System.out.println();

        // 2D GRID PRINTING

        int rows = listRoles.size();
        int columns = allObjects.size();
        twoD = new String[rows][columns];
        int x, y, z = 0;
//        for(x = 0; x < rows; x++) {

//            for(y = 0; y < columns; y++) {
//                twoD[x][y] = "----";
//                z++;
//            }
//        }

        for(x = 0; x < rows; x++) {

            System.out.print(listRoles.get(x));
            System.out.print("\t");
            for (y = 0; y < columns; y++) {

                //twoD[x][y] = "----";
                System.out.print(twoD[x][y] + "\t");
            }
            System.out.println();
        }

        System.out.println("\nRead permissionsToRoles.txt: and assign to permissionHash HashMap.");

        File readPermissionToRoles = new File("permissionsToRoles.txt");
        BufferedReader brPermissionToRoles = new BufferedReader(new FileReader(readPermissionToRoles));
        String permissionRolesString;
        String[] permissionRolesArray;

        while((permissionRolesString = brPermissionToRoles.readLine()) != null) {

            permissionRolesArray = permissionRolesString.split("\\s+");
            grantRoles.add(permissionRolesArray[0]);
            grantAccessRights.add(permissionRolesArray[1]);
            grantObjects.add(permissionRolesArray[2]);
        }

        for(int k = 0; k < grantRoles.size(); k++) {

            assignToGrid(grantRoles.get(k), grantAccessRights.get(k), grantObjects.get(k));

            permissionHash.computeIfAbsent(grantObjects.get(k), rightsList -> new ArrayList<>()).add(grantAccessRights.get(k));

            // prevent duplicates before adding to HasMap List
            if (roleHierarchy.get(grantObjects.get(k)).descendantName != null) {

                if(!permissionHash.get(roleHierarchy.get(grantObjects.get(k)).descendantName).contains(grantAccessRights.get(k))) {

                    permissionHash.computeIfAbsent(roleHierarchy.get(grantObjects.get(k)).descendantName, rightsList -> new ArrayList<>()).add(grantAccessRights.get(k));
                }
                if(!permissionHash.get(roleHierarchy.get(grantObjects.get(k)).descendantName).contains("own")) {

                    permissionHash.computeIfAbsent(roleHierarchy.get(grantObjects.get(k)).descendantName, rightsList -> new ArrayList<>()).add("own");
                }
                if(!permissionHash.get(grantObjects.get(k)).contains("control")) {

                    permissionHash.computeIfAbsent(grantObjects.get(k), rightsList -> new ArrayList<>()).add("control");
                }
                //permissionHash.computeIfAbsent(grantObjects.get(k), rightsList -> new ArrayList<>()).add("control");
                if(!permissionHash.get(roleHierarchy.get(grantObjects.get(k)).descendantName).contains("control")) {

                    permissionHash.computeIfAbsent(roleHierarchy.get(grantObjects.get(k)).descendantName, rightsList -> new ArrayList<>()).add("control");
                }
//
                assignToGrid(grantObjects.get(k), grantAccessRights.get(k), roleHierarchy.get(grantObjects.get(k)).descendantName);
            }

            // Print Role Permission HashMap Values round by round
//            System.out.println(permissionHash);
        }
        // Print final hashMap Permission Values.
        System.out.println(permissionHash);
        //System.out.println(permissionHash.get("R6"));



        // Print 2nd Grid
        System.out.println();
        System.out.print("\t");
        for(String element4: allObjects) {

            System.out.print(element4 + "\t\t");
        }
        System.out.println();
        for(x = 0; x < rows; x++) {

            System.out.print(listRoles.get(x));
            System.out.print("\t");
            for (y = 0; y < columns; y++) {

                System.out.print(twoD[x][y] + "\t");
            }
            System.out.println();
        }
        //Print 2D Array
//        System.out.println();
//        for(int i = 0; i < twoD.length; i++) {
//            System.out.println(Arrays.toString(twoD[i]));
//        }

        System.out.println("\nRead roleSetsSSD.txt: and assign to roleSets HashMap.");

        File readRoleSetsSSD = new File("roleSetsSSD.txt");
        BufferedReader brRoleSets = new BufferedReader(new FileReader(readRoleSetsSSD));
        String roleSetsSSDString;
        String[] roleSetsSSDArray;
        int roleSetsInt;
        int counter = 1;
        while((roleSetsSSDString = brRoleSets.readLine()) != null) {

            roleSetsSSDArray = roleSetsSSDString.split("\\s+");
            roleSetsInt = Integer.parseInt(roleSetsSSDArray[0]);
            //System.out.println(roleSetsInt);

            for(int a = 1; a < roleSetsSSDArray.length; a ++) {
                roleSets.computeIfAbsent(roleSetsInt, usersList -> new ArrayList<>()).add(roleSetsSSDArray[a]);
            }
            System.out.println("Constraint " + counter + ", n = " + roleSetsInt + ", set of roles = " + roleSets.get(roleSetsInt));
            counter ++;
        }
        System.out.println();


        System.out.println("\nRead userRoles.txt: and assign to userRoles HashMap.");

        File readUserRoles = new File("userRoles.txt");
        BufferedReader brUserRoles = new BufferedReader(new FileReader(readUserRoles));
        String userRolesString;
        String[] userRolesArray;
        while((userRolesString = brUserRoles.readLine()) != null) {

            userRolesArray = userRolesString.split("\\s+");
            for(int a = 1; a < userRolesArray.length; a ++) {
                userRoles.computeIfAbsent(userRolesArray[0], usersList -> new ArrayList<>()).add(userRolesArray[a]);
            }
        }
        System.out.println(userRoles + "\n");


        Scanner userReader = new Scanner(System.in);  // Reading from System.in
        System.out.println("Please enter the user in your query: ");
        String input = userReader.next(); // Scans the next token of the input as an int.
        System.out.println("Please enter the object in your query: ");
        String queryObject = userReader.next();
        //System.out.println(queryObject);
        if(userRoles.containsKey(input)) {
            System.out.println("User: " + input + " = " + userRoles.get(input));
        } else {
            System.out.println("invalid user, try again. ");
        }
        if(permissionHash.containsKey(queryObject)) {
            System.out.println("Object: " + queryObject + " = " +  permissionHash.get(queryObject));
        } else {
            System.out.println("invalid object, try again. ");
        }
        //System.out.println(userRoles.get(input));
        //System.out.println(input);
        userReader.close();

//        Scanner objectReader = new Scanner(System.in);  // Reading from System.in
//        System.out.println("Please enter the object in your query: ");
//        Integer userInput = objectReader.nextInt(); // Scans the next token of the input as an int.
//        if(permissionHash.containsKey(input2)) {
//            System.out.println(permissionHash.get(input2));
//        } else {
//            System.out.println("invalid object, try again. ");
//        }
        //System.out.println(permissionHash.get(userInput));
        //System.out.println(userRoles.get(input));
        //System.out.println(userInput);
//        objectReader.close();

    }


    public static void assignToGrid(String roles, String accessRights, String objects) {

        twoD[listRoles.indexOf(roles)][allObjects.indexOf(objects)] = accessRights;
    }

    public static void promptEnterKey(){
        System.out.println("Press \"ENTER\" to continue...");
        try {
            int keyboardRead = System.in.read(new byte[2]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void promptQueryKey(){
//        System.out.println("Please enter the user in your query: ");
//        try {
//            String queryInput = System.in.read("");
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static void printTree(String roleName, int row) throws NullPointerException {
        row++;
        System.out.println(roleName);
        String treeValues;
        ArrayList<Node> ascendants = roleHierarchy.get(roleName).ascendant;
        if (!ascendants.isEmpty()) {
            for (int i = 0; i < ascendants.size(); i++) {
                if ((i + 1) == ascendants.size()) {
                    treeValues = "└──";
                } else {
                    treeValues = "├──";
                }
                if (row > 1) {
                    String indent = String.format("%" + ((row - 1) * 3) + "s", " ");
                    System.out.print(indent + treeValues);
                } else {
                    System.out.print(treeValues);
                }
                String ascendant = roleHierarchy.get(ascendants.get(i).roleName).roleName;
                printTree(ascendant, row);
            }
        }
    }
}