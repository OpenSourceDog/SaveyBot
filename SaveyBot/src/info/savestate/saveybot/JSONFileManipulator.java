/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.savestate.saveybot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.json.*;

/**
 * A database manipulator for SaveyBot's JSON database.
 * @author Joseph El-Khouri
 */
public class JSONFileManipulator {
    
    private final String filename;
    private final Random rand;
    
    public JSONFileManipulator(String filename) {
        this.filename = filename;
        this.rand = new Random('d' + 'o' + 'n' + 'g');
    }
    
    public String getSlot(String slotString, boolean largeResponse) {
        try {
            return getSlot( new BigInteger(slotString));
        } catch (NumberFormatException nfe) {
            // going to load all slots made by user.
        }
        JSONArray json = getJSON();
        if (!largeResponse) {
            int entries = 0;
            for (int i=0; i<json.length(); i++) {
                JSONObject o = json.getJSONObject(i);
                if (o == null) continue;
                if (o.getString("name").equals(slotString)) entries++;
            } 
            if (entries > 0)
                return slotString + " owns " + entries + " savestates!!! :D/";
            return slotString + " doesnt own any savestates );";
        }
        StringBuilder slots = new StringBuilder();
        int entries = 0;
        for (int i=0; i<json.length(); i++) {
            JSONObject o = json.getJSONObject(i);
            if (o == null) continue;
            if (o.getString("name").equals(slotString)) {
                entries++;
                slots.append(o.getString("slot")).append(", ");
            }
        }
        if (entries > 0) {
            slots.deleteCharAt(slots.length()-1);
            slots.deleteCharAt(slots.length()-1);
            return "owha! " + slotString + " owns slot(s) " + slots.toString() + "!!!! :D :D :D/";
        }
        return slotString + " doesn't own any savestates!! (u should fix that !! O:)";
    }
    
    public String markOf(String username) {
        JSONArray json = getJSON();
        ArrayList<String> words = new ArrayList<>();
        if (username.isEmpty()) {
            for (int i=0; i<json.length(); i++) {
                String[] splitMessage = json.getJSONObject(i).getString("message").split("\\s+");
                words.addAll(Arrays.asList(splitMessage));
            }
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<6; i++) /**/ sb.append(words.get(rand.nextInt(words.size()))).append(' ');
            return sb.toString().trim();
        }
        for (int i=0; i<json.length(); i++) {
            JSONObject savestate = json.getJSONObject(i);
            if (savestate.getString("name").equals(username)) {
                String[] splitMessage = savestate.getString("message").split("\\s+");
                words.addAll(Arrays.asList(splitMessage));
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<5; i++) /**/ sb.append(words.get(rand.nextInt(words.size()))).append(' ');
        return sb.toString().trim();
    }
    
    public String markOf() {
        return markOf("");
    }

    private String getSlot(BigInteger slot) {
        JSONArray json = getJSON();
        for (int i=0; i<json.length(); i++) {
            JSONObject o = json.getJSONObject(i);
            if (o == null) continue;
            BigInteger current = new BigInteger(o.getString("slot"));
            if (current.equals(slot))
                return o.getString("message");
        }
        return "that savestate doesnt exist !!! (u should make it)";
    }
    
    public String lowestSlot() {
        BigInteger lowest = BigInteger.ZERO;
        JSONArray json = getJSON();
        boolean passed = false;
        while (!passed) {
            passed = true;
            for (int i=0; i<json.length(); i++) {
                JSONObject o = json.getJSONObject(i);
                BigInteger current = o.getBigInteger("slot");
                if (current.compareTo(lowest) == 0) {
                    lowest = lowest.add(BigInteger.ONE);
                    passed = false;
                    break;
                }
            }
        }
        return lowest.toString();
    }

    public String saveSlot(String username, String message) {    
        return saveSlot(lowestSlot(), username, message);
    }
    
    public String saveSlot(String slotString, String username, String message) {
        BigInteger slot;
        try {
            slot = new BigInteger(slotString);
        } catch (NumberFormatException nfe) {
            return "lmao bye af thats not a real number";
        }
        JSONArray json = getJSON();
        for (int i=0; i<json.length(); i++) {
            JSONObject o = json.getJSONObject(i);
            if (o == null) continue;
            BigInteger current = new BigInteger(o.getString("slot"));
            if (current.equals(slot)) return "waohwo!!! " + o.getString("name") + " owns this savestate you dong !!";
        }
        JSONObject o = new JSONObject();
        o.put("name", username);
        o.put("slot", slot.toString());
        o.put("message", message);
        json.put(o);
        writeJSON(json);
        return "ur savestate was sav'd to slot " + slot.toString() + "! ^O^";
    }
    
    public String randomLoad(String username) {
        JSONObject savestate;
        int slot = -1;
        JSONArray json = getJSON();
        if (username.isEmpty()) {
            slot = rand.nextInt(json.length());
            savestate = json.getJSONObject(slot);
        } else {
            JSONArray userArray = new JSONArray();
            for(int i=0; i<json.length(); i++) {
                if (json.getJSONObject(i).getString("name").equals(username))
                    userArray.put(json.getJSONObject(i));
            }
            if (userArray.length() == 0)
                return username + " doesn't have any savestates!!! O:";
            slot = rand.nextInt(userArray.length());
            savestate = userArray.getJSONObject(slot);
            
        }
        return username + "[" + savestate.getString("slot") + "]: " + savestate.getString("message");
    }
    
    public String randomLoad() {
        return randomLoad("");
    }
    
    /**
     * 
     * @return Returns a JSON Array of the JSON database
     */
    private JSONArray getJSON() {
        File file = new File(filename);
        InputStream stream;
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            stream = null;
        }
        JSONTokener tokenizer = new JSONTokener(stream);
        return new JSONArray(tokenizer);
    }
    
    /**
     * Write the modified JSON Database!
     */
    private void writeJSON(JSONArray json) {
        try {
            String jsonString = json.toString(1);
            byte[] jsonBytes = jsonString.getBytes(Charset.defaultCharset());
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(jsonBytes);
            fos.close();
            System.out.println("JSON database saved to " + filename);
        } catch (IOException e) {
            System.out.println("JSON database error.");
        }
    }
    
}
