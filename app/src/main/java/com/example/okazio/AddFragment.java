
package com.example.okazio;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddFragment#} factory method to
 * create an instance of this fragment.
 */
public class AddFragment extends Fragment {

    private EditText designation ,quantite;
    private Spinner etat;
    private Button saveButton,cancelButton;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String API_URL = "https://okazio.onrender.com";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@Nullable View view,@Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        designation = view.findViewById(R.id.designation);
        quantite = view.findViewById(R.id.quantite);
        etat = view.findViewById(R.id.etat);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        saveButton.setOnClickListener(v -> handleSave());
        cancelButton.setOnClickListener(v -> clearForm());
    }

    private void handleSave(){
        String designationText = designation.getText().toString().trim();
        String quantiteText = quantite.getText().toString().trim();
        String selectState = etat.getSelectedItem() != null ? etat.getSelectedItem().toString():"";

        // validation
        if (designationText.isEmpty()){
            designation.setError("Veuillez entrer un nom de matériel");
            designation.requestFocus();
            return;
        }
        if (selectState.isEmpty() || selectState.equals("--Sélectionner un état--")){
            Toast.makeText(getContext(),"Veuillez entrer un état",Toast.LENGTH_SHORT).show();
            return;
        }
        if (quantiteText.isEmpty()){
            quantite.setError("Veuillez entrer une quantité");
            quantite.requestFocus();
            return;
        }

        int quantiteInt;
        try {
            quantiteInt = Integer.parseInt(quantiteText);
            if (quantiteInt <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e){
            quantite.setError("Veuillez entrer un nombre valide");
            quantite.requestFocus();
            return;
        }

        saveButton.setEnabled(false);
        saveButton.setText("Enregistrement ...");
        cancelButton.setEnabled(false);

        // appel API
        executor.execute(() -> {
            String result = null;
            boolean success = false;

            try {
                JSONObject body = new JSONObject();
                body.put("design", designationText);
                body.put("state", selectState.toLowerCase());
                body.put("quantity", quantiteInt);

                result = postJson(API_URL + "/product", body.toString());
                Log.d("API_RESPONSE", "Réponse : " + result);

                success = true;
            } catch (Exception e) {
                result = e.getMessage();
            }

            final String finaleResult = result;
            final boolean finaleSuccess = success;

            requireActivity().runOnUiThread(() -> {
                saveButton.setEnabled(true);
                saveButton.setText(R.string.btn_enregistrer);
                cancelButton.setEnabled(true);

                if (finaleSuccess){
                        Toast.makeText(getContext(), "Enregistrement réussi", Toast.LENGTH_SHORT).show();
                        clearForm();

                        BottomNavigationView nav =
                                requireActivity().findViewById(R.id.bottom_navigation);
                        if (nav != null) {
                            nav.setSelectedItemId(R.id.nav_list);
                        }

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, new ListFragment())
                                .commit();

                }else {
                    Toast.makeText(getContext(),"Enregistrement échoué",Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String postJson(String apiurl, String json) throws IOException {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(apiurl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15_000);
            conn.setReadTimeout(15_000);

            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")){
                writer.write(json);
                writer.flush();
            }

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder sb = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))){
                String line;
                while ((line = br.readLine()) != null){
                    sb.append(line);
                }
            }

            if (status < 200 || status >= 300){
                throw new IOException(sb.toString());
            }

            return sb.toString();
        } finally {
            if (conn != null){
                conn.disconnect();
            }
        }
    }
    private void clearForm(){
        designation.setText("");
        quantite.setText("");
        etat.setSelection(0);
        designation.requestFocus();
    }

    @Override
    public  void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}