package rwl.bibliassimples.bibliaacf;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Principal extends Fragment {


    ArrayList<String> verses;
    ArrayList<String> books;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_principal, container, false);
        ListView listViewBible = view.findViewById(R.id.listViewBible);
        TextView livro = view.findViewById(R.id.textLivro);
        TextView capitulo = view.findViewById(R.id.textNumeroCapitulo);

        SharedPreferences bibleJsonString = getContext().getSharedPreferences("biblia", Context.MODE_PRIVATE);

        if( bibleJsonString.getString("jsonString", null) == null) {
            String jsonString;
            try {
                InputStream is = getActivity().getAssets().open("acf.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                jsonString = new String(buffer, "UTF-8");
                bibleJsonString.edit().putString("jsonString", jsonString).apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        verses = new ArrayList<>();
        AdapterListBibleSelected adapterListVerses = new AdapterListBibleSelected(getActivity(), verses, view);
        displayBible(listViewBible, adapterListVerses);

        try {
            int lastBook = Integer.parseInt(bibleJsonString.getString("lastBook", "0"));
            int lastChapter = Integer.parseInt(bibleJsonString.getString("lastChapter", "0"));
            JSONArray jsonArray = new JSONArray(bibleJsonString.getString("jsonString", null));

            livro.setText(jsonArray.getJSONObject(lastBook).getString("name"));
            capitulo.setText(String.valueOf(lastChapter+1));

            livro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogSelectBible dialogSelectBible = new DialogSelectBible();
                    dialogSelectBible.show(getActivity().getSupportFragmentManager(), "nada");
                }
            });



        } catch (JSONException e ){

        }

        return view;
    }

    private void displayBible( ListView listView, AdapterListBibleSelected adapterListVerses) {

        try {
            SharedPreferences bibleJsonString = getContext().getSharedPreferences("biblia", Context.MODE_PRIVATE);
            int lastBook = Integer.parseInt(bibleJsonString.getString("lastBook", "0"));
            int lastChapter = Integer.parseInt(bibleJsonString.getString("lastChapter", "0"));

            JSONArray jsonArray = new JSONArray(bibleJsonString.getString("jsonString", null));
            JSONArray allVerseInChaphter = jsonArray.getJSONObject(lastBook).getJSONArray("chapters").getJSONArray(lastChapter);


            for (int i = 0; i < allVerseInChaphter.length(); i++) {

                verses.add(allVerseInChaphter.getString(i));
                listView.setAdapter(adapterListVerses);
                adapterListVerses.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public class AdapterListBibleSelected extends BaseAdapter {

        Activity activity;
        ArrayList<String> verses;
        LayoutInflater inflater;
        View theView;
        public AdapterListBibleSelected (Activity activity, ArrayList<String> verses, View theView){
            this.activity = activity;
            this.verses = verses;
            this.theView = theView;

        }
        @Override
        public Object getItem(int position) {
            return this.verses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return (long) position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(inflater == null ){
                inflater = this.activity.getLayoutInflater();
            }
            if( convertView ==null ){
                convertView = inflater.inflate(R.layout.layoutversiculos, null);
            }

            TextView textCapitulo = convertView.findViewById(R.id.textCapitulo);
            TextView textVersiculo = convertView.findViewById(R.id.textVersiculo);

            textCapitulo.setText(String.valueOf(position +1 ));
            textVersiculo.setText(verses.get(position));

            return convertView;
        }

        @Override
        public int getCount() {
            return this.verses.size();
        }
    }


    public static class DialogSelectBible extends DialogFragment {
        ArrayList<String> books;
        ArrayList<String> chapters;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            View viewDialog = inflater.inflate(R.layout.layoutselectbookchapterdialog, null);
            ListView listViewBooks = viewDialog.findViewById(R.id.bookDialogSelect);
            ListView listViewChapters = viewDialog.findViewById(R.id.chapterDialogSelect);
            listViewChapters.setVisibility(View.GONE);


            books = new ArrayList<>();
            AdapterSimpleListBooksBible adapterSimpleListBooksBible = new AdapterSimpleListBooksBible(getActivity(), books, viewDialog);
            displayListBook(listViewBooks, adapterSimpleListBooksBible);
            adapterSimpleListBooksBible.notifyDataSetChanged();

            listViewBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    listViewChapters.setVisibility(View.VISIBLE);
                    listViewBooks.setVisibility(View.GONE);
                    int lastBook = position;
                    chapters = new ArrayList<>();
                    AdapterSimpleListBooksBible adapterSimpleListBooksBible1 = new AdapterSimpleListBooksBible(getActivity(), chapters, viewDialog);
                    displayListChapter(listViewChapters, adapterSimpleListBooksBible1, lastBook);
                    adapterSimpleListBooksBible1.notifyDataSetChanged();

                    listViewChapters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            SharedPreferences bibleJsonString = getContext().getSharedPreferences("biblia", Context.MODE_PRIVATE);
                            bibleJsonString.edit().putString("lastBook", String.valueOf(lastBook)).apply();
                            bibleJsonString.edit().putString("lastChapter", String.valueOf(position)).apply();
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.replace(R.id.nav_host_fragment_1, new Principal());
                            transaction.commit();
                        }
                    });
                }
            });

            return viewDialog;
        }

        public  class AdapterSimpleListBooksBible extends BaseAdapter {

            Activity activity;
            ArrayList<String> books;
            LayoutInflater inflater;
            View theView;

            public AdapterSimpleListBooksBible (Activity activity, ArrayList<String> books, View theView){
                this.activity = activity;
                this.books = books;
                this.theView = theView;

            }
            @Override
            public Object getItem(int position) {
                return this.books.get(position);
            }

            @Override
            public long getItemId(int position) {
                return (long) position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if(inflater == null ){
                    inflater = this.activity.getLayoutInflater();
                }
                if( convertView ==null ){
                    convertView = inflater.inflate(R.layout.simplelistview, null);
                }

                TextView textCapitulo = convertView.findViewById(R.id.textSimple);

                textCapitulo.setText(books.get(position));

                return convertView;
            }

            @Override
            public int getCount() {
                return this.books.size();
            }
        }

        private void displayListBook( ListView listView, AdapterSimpleListBooksBible adapterSimpleListBooksBible) {

            try {
                SharedPreferences bibleJsonString = getContext().getSharedPreferences("biblia", Context.MODE_PRIVATE);

                JSONArray jsonArray = new JSONArray(bibleJsonString.getString("jsonString", null));

                for (int i = 0; i < jsonArray.length(); i++) {

                    books.add(jsonArray.getJSONObject(i).getString("name"));
                    listView.setAdapter(adapterSimpleListBooksBible);
                    adapterSimpleListBooksBible.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                Toast.makeText(getContext(), "oxe", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        private void displayListChapter( ListView listView, AdapterSimpleListBooksBible adapterSimpleListBooksBible, int position) {

            try {
                SharedPreferences bibleJsonString = getContext().getSharedPreferences("biblia", Context.MODE_PRIVATE);

                JSONArray jsonArray = new JSONArray(bibleJsonString.getString("jsonString", null)).getJSONObject(position).getJSONArray("chapters");
                for (int i = 0; i < jsonArray.length(); i++) {

                    chapters.add(String.valueOf(i+1));
                    listView.setAdapter(adapterSimpleListBooksBible);
                    adapterSimpleListBooksBible.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                Toast.makeText(getContext(), "oxe", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

    }
}