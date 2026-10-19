public abstract class User {
    private int id;
    private String role;
    private String idNumber;
    private String name;
    private String email;
    private String password;
    private String type;
    private String organization;
    private boolean active;

    public User(int id, String role, String idNumber, String name, String email,
            String password, String type, String organization, boolean active) {
        this.id = id;
        this.role = role;
        this.idNumber = idNumber;
        this.name = name;
        this.email = email;
        this.password = password;
        this.type = type;
        this.organization = organization;
        this.active = active;
    }

    public int getId() { return id; }
    public String getRole() { return role; }
    public String getIdNumber() { return idNumber; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getType() { return type; }
    public String getOrganization() { return organization; }
    public boolean isActive() { return active; }

    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
    public void setType(String type) { this.type = type; }
    public void setOrganization(String organization) { this.organization = organization; }
    public void setActive(boolean active) { this.active = active; }

    public String toFileLine() {
        return id + ";" + role + ";" + idNumber + ";" + name + ";" + email + ";"
                + password + ";" + type + ";" + organization + ";" + active;
    }

    // Runs this role's menu and returns the next user (Guest on logout, null to exit)
    public abstract User displayMenu();
}
