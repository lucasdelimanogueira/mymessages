package com.example.mymessages.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.mymessages.R;
import com.example.mymessages.config.ConfigFirebase;
import com.example.mymessages.fragment.ChatFragment;
import com.example.mymessages.fragment.ContactsFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity implements ChatFragment.OnFragmentInteractionListener, ContactsFragment.OnFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth authentication;
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authentication = ConfigFirebase.getFirebaseAuth();

        //criar toolbar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitle("My Messges");
        setSupportActionBar(toolbar);

        //configurar abas
        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                .add("Conversas", ChatFragment.class)
                .add("Contatos", ContactsFragment.class)
                .create()
        );
        final ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);

        //configuracao do searchview
        searchView = findViewById(R.id.materialSearch);

        //listener para botao voltar (sair da pesquisa)
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                //recarregar todas as conversas quando fechar a pesquisa
                ChatFragment fragment = (ChatFragment)adapter.getPage(0);
                fragment.updateConversations();
            }
        });

        //listener para caixa de texto
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //verificar se o usuário está pesquisando em Conversas ou Contatos
                //a partir da tab ativa
                switch (viewPager.getCurrentItem()){
                    case 0://conversas

                        ChatFragment chatFragment = (ChatFragment) adapter.getPage(0);
                        //verificar se há algo digitado na barra de pesquisa
                        if(newText != null && !newText.isEmpty()){
                            //metodo para pesquisar as conversas, com parametro o texto digitado
                            chatFragment.searchConversations(newText.toLowerCase());
                        }else{//usuario nao digitou nada e conversa vazia
                            chatFragment.updateConversations();
                        }
                        break;

                    case 1://contatos

                        ContactsFragment contactsFragment = (ContactsFragment) adapter.getPage(1);
                        //verificar se há algo digitado na barra de pesquisa
                        if(newText != null && !newText.isEmpty()){
                            //metodo para pesquisar as conversas, com parametro o texto digitado
                            contactsFragment.searchContacts(newText.toLowerCase());
                        }else{//usuario nao digitou nada e conversa vazia
                            contactsFragment.updateContacts();
                        }
                }
                return true;
            }
        });
    }

    //criar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        //configurar botao de pesquisa
        MenuItem item = menu.findItem(R.id.menu_search);
        searchView.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    //acoes ao clicar nos itens do menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            //clicar em sair
            case R.id.menu_exit:
                userSignOut();
                finish();
                break;

            //clicar em configurações
            case R.id.menu_config:
                //abrir tela de configurações
                openConfigActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //deslogar usuário
    public void userSignOut(){
        try{
            authentication.signOut();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //abrir configurações
    public void openConfigActivity(){
        Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
        startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}
