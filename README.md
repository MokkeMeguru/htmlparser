# niconico 大百科データ前処理ツール
これは niconico 大百科のデータのうち、 **HTMLのデータを前処理する** ツールです。

## requirement
- leiningen > 2.0.0
- sqlite > 2.8.0

## Usage

1. 次のような形式でダウンロードしたデータを解凍してください。

```text
.
└── nico-dict
    └── zips
        ├── download.txt
        ├── head
        │   ├── head2008.csv
        │   ├── ...
        │   └── head2014.csv
        ├── head.zip
        ├── res
        │   ├── res2008.csv
        │   ├── ...
        │   └── res2014.csv
        ├── res.zip
        ├── rev2008.zip
        ├── rev2009
        │   ├── rev200901.csv
        │   ├── rev200902.csv
        │   ├── rev200903.csv
        │   ├── ...
        │   └── rev200912.csv
        ├── rev2009.zip
        ├──...
        ├── rev2013.zip
        ├── rev2014
        │   ├── rev201401.csv
        │   └── rev201402.csv
        └── rev2014.zip
```

TODO: 実行のためのシェルファイル作成

2. すべての csv ファイルが格納されているフォルダについて次のコマンドを実行してください。
```
find . -type f -name "*.csv" -print0 | xargs -0 sed -i 's/\\"/""/g'
find . -type f -name "*.csv" -print0 | xargs -0 sed -i 's/\\""/\\"/g'
```

上はデータのCSVの形式を修正するためのシェルコマンドです。

TODO: 実行のためのシェルファイル作成

3. 次のコマンドを実行してください。

```shell
lein run preprocess-raw-data --source "/<your dictionary>/nico-dict/zips/"
```


## Preprocessed file?
see. [example edn file](./example.edn)

実行後は上のファイルの形式に変換されたHTMLのデータが、 nippy の形式で `resources/revXXXXXX.npy` の形に保存されます。大体6GBくらいのサイズになります。計算機にもよりますが実行時間は30分未満です。

~zip されたファイルは [このGoogleDriveへアップロードされています](https://drive.google.com/file/d/1amt99PIlBjWlzrmh-Uvr55dS6aRqXgB2/view?usp=sharing)~

## HTML の解析
ref [advanced document](./doc/adv-doc.org)

## License

Copyright © 2019 MokkeMeguru

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
