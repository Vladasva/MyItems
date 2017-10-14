package com.example.vladasverkelis.myitems.data;

/**
 * Created by vladasverkelis on 20/09/2017.
 */

public class Item {


    /**
     * Private field for the item's name
     */
    private final String name;

    /**
     * Private field for the item's quantity
     */
    private final int quantity;

    /**
     * Private field for the item's price
     */
    private final String price;

    /**
     * Private field for the item's image path
     */
    private final String image;

    /**
     * Private field for the item's supplier
     */
    private final String supplier;

    /**
     * Private field for the item's email
     */
    private final String email;

    /**
     * Private field for the item's phone
     */
    private final String phone;


    public Item(String name, String price, int quantity, String supplier, String phone, String email, String image){

        this.name =name;
        this.quantity = quantity;
        this.price = price;
        this.supplier = supplier;
        this.phone = phone;
        this.email = email;
        this.image = image;
    }

    /**
     * Returns item's name
     */
    public String getName(){
        return name;
    }

    /**
     * Returns item's quantity
     */
    public int getQuantity(){
        return quantity;
    }

    /**
     * Returns item's price
     */
    public String getPrice(){
        return price;
    }

    /**
     * Returns item's supplier
     */
    public String getSupplier(){
        return supplier;
    }

    /**
     * Returns item's phone
     */
    public String getPhone(){
        return phone;
    }

    /**
     * Returns item's email
     */
    public String getEmail(){
        return email;
    }

    /**
     * Returns item's image
     */
    public String getImage(){
        return image;
    }

    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", quantity=" + quantity +
                ", supplier='" + supplier + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
