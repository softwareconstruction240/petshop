package model;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;

public class PetList extends ArrayList<Pet> {
    private static final Gson GSON = new Gson();

    public PetList() {

    }

    public PetList(Collection<Pet> pets) {
        super(pets);
    }

    public String toJson() {
        return GSON.toJson(this.toArray());
    }

    public String toString() {
        return toJson();
    }
}
