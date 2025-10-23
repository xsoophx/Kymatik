# python
import argparse
import csv
import os
import sys

import matplotlib.pyplot as plt

def read_csv(path):
    times = []
    values = []
    with open(path, newline='') as f:
        reader = csv.DictReader(f)
        for row in reader:
            try:
                times.append(float(row.get('timeSec') or row.get('time') or row.get('time_sec')))
                values.append(float(row.get('value')))
            except (TypeError, ValueError):
                continue
    return times, values

def default_output_for(csv_path):
    dir_ = os.path.dirname(csv_path)
    base = os.path.splitext(os.path.basename(csv_path))[0]
    return os.path.join(dir_, base + ".png")

def plot_and_save(times, values, out_path, show=False):
    plt.figure(figsize=(10, 4))
    plt.plot(times, values, linewidth=0.8)
    plt.xlabel("time (s)")
    plt.ylabel("value")
    plt.title("CSV Data Plot")
    plt.grid(alpha=0.3)
    plt.tight_layout()
    plt.savefig(out_path, dpi=300)
    if show:
        plt.show()
    plt.close()

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    default_csv = os.path.abspath(os.path.join(script_dir, '..', '..', 'kotlin', 'cc', 'suffro', 'bpmanalyzer', 'csv', 'resources', 'starting_position_output.csv'))
    default_out = os.path.abspath(os.path.join(script_dir, '..', 'resources', 'csv_data.png'))

    parser = argparse.ArgumentParser(description="Plot image out of CSV data")
    parser.add_argument("csv", nargs='?', default=default_csv)
    parser.add_argument("out", nargs='?', default=default_out, help="the output image path")
    parser.add_argument("--show", action="store_true", help="show the plot after saving")

    args = parser.parse_args()

    csv_path = args.csv
    if not os.path.isfile(csv_path):
        print(f"Couldn't find CSV: {csv_path}", file=sys.stderr)
        sys.exit(2)

    out_path = args.out or default_output_for(csv_path)
    times, values = read_csv(csv_path)
    if not times or not values:
        print("Keine gültigen Daten in der CSV gefunden.", file=sys.stderr)
        sys.exit(3)

    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    plot_and_save(times, values, out_path, show=args.show)
    print(f"Bild gespeichert: {out_path}")

if __name__ == "__main__":
    main()
