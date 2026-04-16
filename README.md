
---

# EmoGraph (エモグラフ) 📊🧠

**「自分のトリセツ」をAIと一緒に作る、新感覚の感情ログアプリ。**

### 🌟 どんなアプリ？
「今日、なんか気分がいいな」「なぜかイライラする」……そんな言葉にできない心の動きを「数値」にして、その日の「天気・気温」と一緒に記録するアプリです。
データが溜まったら、AI（NotebookLMなど）に読み込ませるだけ。AIがあなた専用のカウンセラーとして、あなたの傾向を分析してくれます。

---

### 🛠 技術の土台 (Technical Stack)
このアプリは、最新のAndroid開発スタンダードに則って作られています。

* **開発言語: Kotlin (ことり)**
    * Googleが推奨する、Android開発のための最新で安全なプログラミング言語です。
* **開発ツール: Android Studio**
    * プロのエンジニアが世界中で使用している、アプリ作りのための「工房」です。
* **画面作り: Jetpack Compose**
    * スマホの画面をサクサク動かし、直感的に操作できる最新の仕組みです。
* **データの保管箱: Firebase**
    * Googleが提供するクラウドサービス。あなたの記録を安全に保存し、スマホを替えてもデータを守ります。
* **お天気API:**
    * ネットから最新の気象情報を自動で取ってくる「情報窓口」です。

---

### 🗄 データの仕組み (Database Structure)
アプリが何を記録しているのか、その「中身」をわかりやすく整理しました。1つの記録には以下の情報がセットになっています。

| カテゴリ | 記録される内容 |
| :--- | :--- |
| **いつ** | 記録した日付と時間 |
| **感情** | 自分で決めた「感情の名前」と、その「強さ (1〜10)」 |
| **環境** | その時の「天気」と「気温」 |
| **きっかけ** | 何が起きたか (出来事) と、その時考えたこと (メモ) |

---

### 🚀 このアプリが解決すること
「雨の日はやる気が出ない」「気温が25度を超えると焦りやすい」といった、自分では気づきにくい**「環境と心の関係」**をデータで見える化します。

---

このアプリがどのように動いているのか、その「全体像」を視覚的に理解できるツールを作成しました。

<img width="1080" height="2400" alt="Screenshot_2026-04-16-11-20-13-106_com kha98 emograph" src="https://github.com/user-attachments/assets/f0558261-20bd-4ba6-8510-8fd3b970e884" />

<img width="1080" height="2400" alt="Screenshot_2026-04-16-11-53-26-014_com kha98 emograph" src="https://github.com/user-attachments/assets/cf2f0f05-62fa-4ab7-ad48-c1ea34c1c2de" />

<img width="1080" height="2400" alt="Screenshot_2026-04-16-11-20-36-546_com google android apps labs language tailwind" src="https://github.com/user-attachments/assets/90347748-9dea-4db7-8e6a-4969ea28ba34" />
<img width="1080" height="2400" alt="Screenshot_2026-04-16-11-49-03-641_com google android apps labs language tailwind" src="https://github.com/user-attachments/assets/84338bcb-f87c-4fdb-823e-dc9ff1ae2230" />
