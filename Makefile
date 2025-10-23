CSV_CREATOR_MAIN := cc.suffro.bpmanalyzer.csv.CsvCreator
CSV_OUT := src/main/kotlin/cc/suffro/bpmanalyzer/csv/resources/starting_position_output.csv
MUSIC_SAMPLE=./src/test/resources/samples/120bpm_140Hz.wav
PY_SCRIPT := src/main/python/src/draw_csv.py
PNG_OUT := src/main/python/resources/csv_data.png
SAMPLES := 1000

.PHONY: all csv image

all: image

# TODO: adjust command
csv:
	./gradlew run --no-daemon -PmainClass=cc.suffro.bpmanalyzer.csv.CSVCreatorKt \
  	--args="-sp $(MUSIC_SAMPLE) -csv $(CSV_OUT) -size $(SAMPLES)"

image:
	TMP=$$(mktemp /tmp/csv_firstXXXX.csv); \
	( head -n 1 $(CSV_OUT) && tail -n +2 $(CSV_OUT) | head -n $(SAMPLES) ) > $$TMP; \
	.venv/bin/python $(PY_SCRIPT) $$TMP $(PNG_OUT); \
	rm -f $$TMP
