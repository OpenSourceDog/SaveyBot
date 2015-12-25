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
import org.json.*;

/**
 * A database manipulator for SaveyBot's JSON database.
 * @author Joseph El-Khouri
 */
public class JSONFileManipulator {
    
    private final String filename;
    
    public JSONFileManipulator(String filename) {
        this.filename = filename;
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
                return ":D " + slotString + " owns " + entries + " savestates!!!";
            return ")''': " + slotString + " doesnt own any savestates );";
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
        return ")); " + slotString + " doesn't own any saves (u should fix that !!)";
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
        return "the savestate doesnt exist !!! (u should make it)";
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