<h2>Cloudopsys beadandó feladat</h2>

Az alkalmazás az alább felsorolt futtatható modulokból áll. A lényeg, hogy a **cloudopsys-core** feltétlenül kell fusson ahhoz, hogy a többi modul működjön.

<h4>cloudopsys-core</h4>

Ez a modul felelős a műveletek megvalósításáért. Csak ennek a modulnak van adatbázis hozzáférérse és a többi modul őt hívja rest api-clienten keresztül.

<h4>cloudopsys-cli-quickstart</h4>

Egy konzolos quickstart wizardot biztosít, aminek segítségével könnyen létrehozhatjuk az első felhasználónkat és tartozékait.

<h4>cloudopsys-cli-aiprompt</h4>

Szintén egy konzolos alkalmazás, ami promp-utasításokat fogad, amiket a remote LLM segítségével megvalósítható műveletekre fordít és hajt végre.

<h4>cloudopsys-web</h4>

Röptében hoztam létre leginkább azzal a céllal, hogy lássam az aktuális állapotot és, hogy a műveletek sikeresek voltak-e.

<h3>Szükséges konfigurálás</h3>
* cloudopsys-cli-aiprompt az openai keyt egy OPENAI_API_KEY rendszerváltozóban várja
* cloudopsys-core esetén egy in-memory SQLite adatbázis van beállítva, ezt lehet esetleg konkrét

<h3>AI használat</h3>
Én elég sokat használom az AI Agenteket, de jellemzően a minőségi, drágább modelleket fizetem meg saját zsebből.
Ez most a korszellem és kísérletezés része is, de tudok alkalmazkodni az előírtakhoz (visszavenni kicsit a vibe-kódolásból vagy esetleg még többet használni az agenteket).