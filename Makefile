CSV_CREATOR_MAIN := cc.suffro.bpmanalyzer.csv.CsvCreator
MUSIC_SAMPLE=./src/test/resources/samples/120bpm_140Hz.wav
PY_SCRIPT := src/main/python/src/draw_csv.py

# "Normal" samples
SAMPLES := 10000
PNG_OUT := src/main/python/resources/csv_data.png
CSV_OUT := src/main/kotlin/cc/suffro/bpmanalyzer/csv/resources/starting_position_output.csv


# Lowpass
SAMPLES_LP := 100000
PNG_OUT_LP := src/main/python/resources/csv_data_lp.png
CSV_OUT_LP := src/main/kotlin/cc/suffro/bpmanalyzer/csv/resources/starting_position_output_lp.csv

.PHONY: all csv image

all: image

csv:
	./gradlew run --no-daemon -PmainClass=cc.suffro.bpmanalyzer.csv.CSVCreatorKt \
  	--args="-sp $(MUSIC_SAMPLE) -csv $(CSV_OUT) -size $(SAMPLES)"

image:
	TMP=$$(mktemp /tmp/csv_firstXXXX.csv); \
	( head -n 1 $(CSV_OUT) && tail -n +2 $(CSV_OUT) | head -n $(SAMPLES) ) > $$TMP; \
	.venv/bin/python $(PY_SCRIPT) $$TMP $(PNG_OUT); \
	rm -f $$TMP

# Lowpass
csv-lp:
	./gradlew run --no-daemon -PmainClass=cc.suffro.bpmanalyzer.csv.CSVCreatorKt \
  	--args="-sp $(MUSIC_SAMPLE) -csv $(CSV_OUT_LP) -size $(SAMPLES_LP)"

image-lp:
	TMP=$$(mktemp /tmp/csv_firstXXXX.csv); \
	( head -n 1 $(CSV_OUT_LP) && tail -n +2 $(CSV_OUT_LP) | head -n $(SAMPLES_LP) ) > $$TMP; \
	.venv/bin/python $(PY_SCRIPT) $$TMP $(PNG_OUT_LP); \
	rm -f $$TMP
