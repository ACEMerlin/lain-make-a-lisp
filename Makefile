SOURCES = src/main/java/lain/Reader.java src/main/java/lain/Core.java \
               src/main/java/mal/Env.java src/main/java/mal/Lain.java \
               src/main/java/mal/Printer.java src/main/java/mal/ReadLine.java \
               src/main/java/mal/Types.java

all:
	mvn install
	make lain

dist: mal.jar mal

lain.jar: target/classes/lain/Lain.class
	mvn assembly:assembly
	cp target/lain.jar bin/$@

SHELL := bash
lain: lain.jar
	cat <(echo -e '#!/bin/sh\nexec java -jar "$$0" "$$@"') bin/lain.jar > bin/$@
	chmod +x bin/lain

src/main/mal/%.java:
	mvn install

clean:
	mvn clean
	rm -f bin/lain.jar bin/lain

#.PHONY: stats tests $(TESTS)
.PHONY: stats

stats: $(SOURCES)
	@wc $^
	@printf "%5s %5s %5s %s\n" `grep -E "^[[:space:]]*//|^[[:space:]]*$$" $^ | wc` "[comments/blanks]"
