package common;

/**
 * Representa um pedido enviado pelo cliente ao servidor.
 */
public class ClientRequest extends Message{

    //Atributos de login
    private String username;
    private String password;

    //Atributos de atestação
    private String appName;
    private long appSize;

    //Atributos gerais dos comandos
    private String home;
    private String userIdToAdd; 
   
    private String section;
    private String deviceId;
    private int intValue;

    /**
     * Cria um pedido associado a um comando.
     *
     * @param command comando a executar
     */
    public ClientRequest(Command command) {
        super(command);
    }

    
    /**
     * Devolve o username do pedido.
     *
     * @return username enviado
     */
    public String getUsername() {
        return username;
    }

    /**
     * Define o username do pedido.
     *
     * @param username username a guardar
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Devolve a password do pedido.
     *
     * @return password enviada
     */
    public String getPassword() {
        return password;
    }

    /**
     * Define a password do pedido.
     *
     * @param password password a guardar
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Devolve o nome da aplicação usado na atestação.
     *
     * @return nome da aplicação
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Define o nome da aplicação usado na atestação.
     *
     * @param appName nome da aplicação
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    /**
     * Devolve o tamanho da aplicação usado na atestação.
     *
     * @return tamanho do binário
     */
    public long getAppSize() {
        return appSize;
    }

    /**
     * Define o tamanho da aplicação usado na atestação.
     *
     * @param appSize tamanho do binário
     */
    public void setAppSize(long appSize) {
        this.appSize = appSize;
    }

    /**
     * Devolve a casa associada ao pedido.
     *
     * @return nome da casa
     */
    public String getHome() {
        return home;
    }

    /**
     * Define a casa associada ao pedido.
     *
     * @param home nome da casa
     */
    public void setHome(String home) {
        this.home = home;
    }

    /**
     * Devolve o identificador do utilizador a adicionar.
     *
     * @return identificador do utilizador
     */
    public String getUserIdToAdd() {
        return userIdToAdd;
    }

    /**
     * Define o identificador do utilizador a adicionar.
     *
     * @param userIdToAdd identificador do utilizador
     */
    public void setUserIdToAdd(String userIdToAdd) {
        this.userIdToAdd = userIdToAdd;
    }

    /**
     * Devolve a secção associada ao pedido.
     *
     * @return secção indicada
     */
    public String getSection() {
        return section;
    }

    /**
     * Define a secção associada ao pedido.
     *
     * @param section secção a guardar
     */
    public void setSection(String section) {
        this.section = section;
     }
    
    /**
     * Devolve o ID do dispositivo.
     *
     * @return ID do dispositivo
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Define o ID do dispositivo.
     *
     * @param deviceId ID do dispositivo
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Devolve o valor inteiro associado ao pedido.
     *
     * @return valor inteiro enviado
     */
    public int getIntValue() {
        return intValue;
    }

    /**
     * Define o valor inteiro associado ao pedido.
     *
     * @param intValue valor inteiro a guardar
     */
    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

}
