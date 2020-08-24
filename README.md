## Progetto Android Chronotracker App [![relazione](https://img.shields.io/badge/relazione-disponibile%20in%20pdf-brightgreen)](https://github.com/darkimage/Universita-android-ChronotrackerApp/raw/master/documents/relazione_android.pdf)
Applicazione realizzata utilizzando [Android Studio](https://developer.android.com/studio).

Strutturata per utilizzare tutte le tecniche di sviluppo mobile quali:

 - Utilizzo del pattern di programmazione **MVC (Model-View-Controller)** (che e' anche la base del Framework UI di Android)
 - Utilizzo della libreria [**Android Material**](https://material.io/develop/android/) per ottenere una UI moderna, responsiva e conforme alle specifiche del **Material Design**.

l'applicazione realizza una app per il tracciamento e cronometraggio di atleti, mette a disposizione interfaccie per i profili degli altleti e la l 

Traccia Progetto
---------------------

#### Technologies Requirements
 - Database SQLite
 
#### Requirement/Functionalities 
 - Support the following Sports, Activity types
	 - Run 
	 - Cycling 
	 - Swim 
		 - Freestyle swimming 
		 - Butterfly stroke 
		 - Breaststroke 
		 - Backstroke 
 - Create Athletes profile with Name, Surname and Favourite Sport 
 - Before start tracking a training the user should select the Athlete, Sport, Activity Type 
and Distance preference (Meters or Kilometers) 
 - By Clicking Start Tracking the application shows a Timer with the possibility to Pause, Resume and specify Laps
 - Each tracked session contains: 
	 - Athlete reference 
	 - Start time 
	 - Stop time 
	 - Laps 
	 - Distance 
	 - Speed 
 - For each Athlete the application shows the list of associated activities 
 - A Calendar allows to review training for a single specified Day

#### Extra Features 
 - Export all trainings as CSV (without Laps)
 - Export a single training as CSV (with laps if available)

Pattern MVC
----------------
Il pattern di programmazione MVC e' lo standard utilizzato dal Framework Android per la realizzione di interfacce grafiche da presentare all'utente.
la sigla MVC sta per Model View Controller ovvero la sudivisione del codice in differenti componenti ognuno con la propria funzione:

<p align="center">
  <img width="241" height="220" src="https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/MVC-Process.png/220px-MVC-Process.png">
  <p align="right" size="16px"><sub>Wikipedia - public domain</sub></p>
</p>

 - **Model**: fornisce l'accesso ai dati dell'applicazione (Storage, solitamente un database relazionale/non relazionale o anche semplici file)
 - **View**: visualizza i dati contenuti nel model, le view sono struttare in modo da essere indipendenti dai dati che devono visualizzare rendendo cosi possibile riutilizzarle efficentemente. Si occupano anche dell'interazione con l'utente finale.
 - **Controller**: Il controller realizza la gestione dell'input dell'utente e insieme al Model crea la parte del codice chiamata [Business logic](https://it.wikipedia.org/wiki/Business_logic).

Comunicazione tra Controllers
-------------------------------------
Per la comunicazione avviene tramite **Observables (LiveData)** ogni fragment registra e invia i dati utilizzandodifferenti Observables per definire differenti flussi di dati derivanti dalle azioni dell’utente o anche dal recupero di dati dal Database 

**Esempio comunicazione toolbar:**

<p align="center">
  <img width="745" height="445" src="https://github.com/darkimage/Universita-android-ChronotrackerApp/raw/master/documents/Android_toolbar.jpg">
</p>

Implementazione Query Asincrone
------------------------------------------------
Per far usufruire all'utente finale una esperienza fluida senza caricamenti o freeze della UI mentre i dati vengono caricati l'intera interazione tra Controllers e Models e stat realizzata in modo asincrono utilizzando alcune classi scritte ADHOC per questo progetto.

Esempio di funzione asincrona per recuperare le attivita' di un utente:
```java
public void getActivities(DatabaseResult result, DatabaseError error) { 
    NoLeakAsyncTask<Void, Void, Cursor> task = new NoLeakAsyncTask<>( 
            mContext, 
            (Void... voids) -> { 
                SQLiteDatabase db = dbHelper.getWritableDatabase(); 
                Cursor queryCursor = db.query(AppTables.ACTIVITY_TABLE.getName(), 
                        new String[]{ 
                                AppTables.TABLE_ID_COL.getName(), 
                                AppTables.ACTIVITY_TABLE_COL_0.getName()}, 
                        null, 
                        null, 
                        null, 
                        null, 
                        null); 
                return queryCursor; 
            }, 
            (Cursor cursor) -> { 
                result.OnResult(cursor); 
            }, 
            (Exception e) -> { 
                error.OnError(e); 
            } 
    ); 
    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 
```

### Classe NoLeakAsyncTask
La classe NoLeakAsyncTask e’ una estensione della classe del framework android AsynkTask, consente di eseguire una task in background (in un thread differente dal principale) e al suo completamento o fallimento di chiamare metodi di callback nel main thread ovvero quello della UI. 

La classe è stata strutturata per semplificare le operazioni di query asincrone di un database ma essendo stata creata in modo da essere molto flessibile consente di gestire qualsiasi altra operazione che debba essere eseguita in background, in questa app il suo utilizzo e prettamente relativo all’esecuzione di query 

**Cosa distingue questa classe dalla sua classe base *AsynkTask*?** 

La differenza principale risiede nel fatto che questa classe detiene una ``` 
WeakReference<Activity>``` ad una Activity, che è utilizzato nel momento in cui la task di background finisce l’esecuzione per stabile se e presente 
una activity oppure. se la task è sopravvissuta più a lungo della activity che la fatta partire. Nel secondo caso i metodi di callback non vengono eseguiti siccome non esiste una interfaccia grafica da aggiornare siccome l’activity e stata distrutta. 

**Esempio di Flow per una task asincrona:**

```java 
public NoLeakAsyncTask(Activity context, BackgroundTask<I, R> task, PostTask<R> 
postTask, ErrorTask errorTask, ThreadPostTask threadPostTask) 
```
Costruttore numero 4, utilizzo che mostra la flessibilità di questa classe rappresentato dalla funzione addSession riportata [qui](https://github.com/darkimage/Universita-android-ChronotrackerApp/blob/40b34304ec2961469ad21c4107b6cc915dc63f70/app/src/main/java/unipr/luc_af/chronotracker/helpers/Database.java#L74) 
 che fa uso delle database transaction per annullare (tramite un **[ROLLBACK](https://dev.mysql.com/doc/refman/8.0/en/commit.html)**) un eventuale inserimento di una sessione.
 
<p align="center">
  <img width="625" height="528" src="https://github.com/darkimage/Universita-android-ChronotrackerApp/raw/master/documents/android_query_flow.jpg">
</p>
<!--stackedit_data:
eyJoaXN0b3J5IjpbNDQ1NDAyNjg4LC00NjczMjEwMDgsLTEzND
U0NzUxNzZdfQ==
-->