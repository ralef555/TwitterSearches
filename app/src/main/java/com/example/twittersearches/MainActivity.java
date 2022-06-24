package com.example.twittersearches;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    private EditText edtConsulta;
    private EditText edtTag;
    private SharedPreferences preferenciasSalvas;
    private ArrayList<String> tags;
    private ArrayAdapter<String> adapter;
    private static final String SEARCHES = "searches";
    private ImageButton btnSalvar;
    private ListView listaConsulta;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtConsulta = findViewById(R.id.edt_consulta);
        edtTag = findViewById(R.id.edt_tag);
        btnSalvar = findViewById(R.id.btn_salvar);
        listaConsulta = findViewById(R.id.listView);

        preferenciasSalvas = getSharedPreferences(SEARCHES, MODE_PRIVATE);
        tags = new ArrayList<String>(preferenciasSalvas.getAll().keySet());
        Collections.sort(tags, String.CASE_INSENSITIVE_ORDER); //ordena a lista
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                                            android.R.id.text1, tags);
        listaConsulta.setAdapter(adapter);

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!edtConsulta.getText().toString().isEmpty() && !edtTag.getText().toString().isEmpty()){
                    String consulta = edtConsulta.getText().toString();
                    String tag = edtTag.getText().toString();
                    adicionarConsulta(consulta, tag);
                    edtConsulta.getText().clear();
                    edtTag.getText().clear();

                    //codigo para ocultar teclado:
                    ((InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                            edtTag.getWindowToken(), 0);

                }else{
                    Toast.makeText(getApplicationContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //click curto
        listaConsulta.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String tag = ((TextView)view).getText().toString();
                String urlString = getString(R.string.searchURL)+
                        Uri.encode(preferenciasSalvas.getString(tag, ""), "UTF-8");
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(urlString));
                startActivity(webIntent);
            }
        });

        //click longo
        listaConsulta.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String tag = ((TextView)view).getText().toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setItems(R.array.dialog_items, new DialogInterface.OnClickListener() {//dialog_items está no strings.xml
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                compartilharBusca(tag);
                                break;
                            case 1:
                                edtTag.setText(tag);
                                break;
                            case 2:
                                removerBusca(tag);
                                break;

                        }
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.create().show();
                return true;
            }
        });

    }

    public void adicionarConsulta(String query, String tag){
        SharedPreferences.Editor editor = preferenciasSalvas.edit();
        editor.putString(tag, query);
        editor.apply();

        if(!tags.contains(tag)){
            tags.add(tag);
            Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
            adapter.notifyDataSetChanged();
        }

    }

    public void compartilharBusca (String tag){
        String urlString = getString(R.string.searchURL)+
                Uri.encode(preferenciasSalvas.getString(tag, ""), "UTF-8");
        Intent sharerIntent = new Intent();
        sharerIntent.setAction(Intent.ACTION_SEND);
        sharerIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareSubject));
        sharerIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareMessage, urlString));
        sharerIntent.setType("text/plain");
        startActivity(Intent.createChooser(sharerIntent, getString(R.string.shareSearch)));
    }

    public void removerBusca(String tag){
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(MainActivity.this);
        confirmBuilder.setMessage(getString(R.string.confirmMessage, tag));
        confirmBuilder.setNegativeButton("Nao", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        confirmBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                tags.remove(tag);
                SharedPreferences.Editor preferencesEditor = preferenciasSalvas.edit();
                preferencesEditor.remove(tag);
                preferencesEditor.apply();
                adapter.notifyDataSetChanged();
            }
        });
        confirmBuilder.create().show();
    }

}