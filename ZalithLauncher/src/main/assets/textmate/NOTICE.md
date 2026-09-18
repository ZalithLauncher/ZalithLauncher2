# NOTICE

Zalith Launcher 2 includes TextMate grammar and theme files from the following
third-party projects. The files are redistributed under the MIT License, the
full text of which appears at the end of this notice.

1. Visual Studio Code — https://github.com/microsoft/vscode
   Copyright (c) Microsoft Corporation.
   Files: c.tmLanguage.json, cpp.tmLanguage.json, css.tmLanguage.json,
   html.tmLanguage.json, java.tmLanguage.json, javascript.tmLanguage.json,
   json.tmLanguage.json, markdown.tmLanguage.json, python.tmLanguage.json,
   shellscript.tmLanguage.json, sql.tmLanguage.json, typescript.tmLanguage.json,
   xml.tmLanguage.json, dark_vs.json, light_vs.json.
   Note: markdown.tmLanguage.json has been locally modified to work around
   tm4e/joni limitations: the `end` patterns of the `bold` and `italic` rules
   are written without backreferences, the `strikethrough` rule uses a
   simplified pattern, and a `fenced_code_block_kotlin` rule was added.

2. vscode-kotlin — https://github.com/fwcd/vscode-kotlin
   Copyright (c) 2016 George Fraser
   Copyright (c) 2018 fwcd
   File: kotlin.tmLanguage.json.

3. vscode-logfile-highlighter — https://github.com/emilast/vscode-logfile-highlighter
   Copyright (c) 2015 emilast
   File: log.tmLanguage.json. The upstream file is in plist format; it was
   converted to JSON with the semantics unchanged.

4. YAML-Syntax-Highlighter — https://github.com/RedCMD/YAML-Syntax-Highlighter
   Copyright (c) 2021 RedCMD
   Files: yaml.tmLanguage.json, yaml-1.2.tmLanguage.json,
   yaml-embedded.tmLanguage.json. The YAML grammar is implemented as layered
   injection grammars: the shell `yaml.tmLanguage.json` references
   `source.yaml.1.2` and `source.yaml.embedded`, which are provided by the two
   additional files. Stray `comment` entries (spec-documentation links) inside
   `beginCaptures` were removed from yaml-1.2.tmLanguage.json and
   yaml-embedded.tmLanguage.json, as tm4e does not accept them.

MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
