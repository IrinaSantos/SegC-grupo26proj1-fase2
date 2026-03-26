package server;

import model.Casa;

/**
 * Encapsula operacoes de criacao e gestao de casas.
 */
public class HouseController {

    private ServerState state;

    /**
     * Cria um controlador associado ao estado do servidor.
     *
     * @param state estado global do servidor
     */
    public HouseController(ServerState state) {
        this.state = state;
    }

    /**
     * Cria uma nova casa para o utilizador dono.
     *
     * @param houseName nome da casa
     * @param owner dono da casa
     * @return código textual com o resultado da operacao
     */
    public String createHouse (String houseName, String owner){
        if(state.houseExists(houseName)){
            return "NOK";
        }

        Casa newHouse = new Casa(houseName, owner);
        state.addCasa(newHouse);
        return "OK";
    }

    /**
     * Adiciona permissão de acesso a uma secção da casa.
     *
     * @param requester utilizador que faz o pedido
     * @param targetUser utilizador que recebe a permissão
     * @param houseName nome da casa
     * @param section secção autorizada
     * @return código textual com o resultado da operacao
     */
    public String addPermission(String requester, String targetUser, String houseName, String section){
        if (!state.houseExists(houseName)) {
            return "NOHM";
        }
        Casa casa = state.getCasa(houseName);
        if (!casa.getOwner().equals(requester)) {
            return "NOPERM";
        }
        casa.addPermission(targetUser, section);
        state.saveData();
        return "OK";
    }

    /**
     * Regista um dispositivo numa secção da casa indicada.
     *
     * @param requester utilizador que pede o registo
     * @param houseName nome da casa
     * @param section secção do dispositivo
     * @return identificador do dispositivo ou código textual de erro
     */
    public String registerDevice(String requester, String houseName, String section){
        if (!state.houseExists(houseName)) {
            return "NOHM";
        }
        Casa casa = state.getCasa(houseName);
        if (!casa.getOwner().equals(requester)) {
            return "NOPERM";
        }

        String deviceId = casa.incrementSectionCounter(section);
        state.saveData();
        return deviceId;
    }
}
