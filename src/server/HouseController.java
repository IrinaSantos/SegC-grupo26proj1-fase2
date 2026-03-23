package server;

import model.Casa;

public class HouseController {

    private ServerState state;

    public HouseController(ServerState state) {
        this.state = state;
    }

    public String createHouse (String houseName, String owner){
        if(state.houseExists(houseName)){
            return "NOK";
        }

        Casa newHouse = new Casa(houseName, owner);
        state.addCasa(newHouse);
        return "OK";
    }

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