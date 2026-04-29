import csv
import random
import string
import sys
from datetime import datetime, timedelta

OUTPUT = sys.argv[1] if len(sys.argv) > 1 else "/home/guilherme-araujo/Downloads/demo (3)/transactions_tamplete.csv"
ROWS = int(sys.argv[2]) if len(sys.argv) > 2 else 1_000_000

DESCRICOES = [
    "Despesa Operacional", "Honorários Consultoria", "Pagamento Fornecedor",
    "Aluguel Escritório", "Serviço de TI", "Material de Escritório",
    "Energia Elétrica", "Telefonia", "Internet", "Seguro Empresarial",
    "Manutenção Predial", "Transporte", "Alimentação", "Marketing Digital",
    "Licença Software", "Folha de Pagamento", "INSS", "FGTS",
    "Imposto de Renda", "ISS", "Receita Venda Produto", "Receita Serviço",
    "Juros Recebidos", "Dividendos", "Reembolso", "Devolução",
]
CONTAS = ["Bradesco", "Itau", "Banco do Brasil", "Santander", "Caixa", "Nubank", "Inter", "Safra"]
TIPOS = ["Receita", "Despesa"]

BASE_DATE = datetime(2024, 1, 1)
DATE_RANGE = 900  # ~2.5 anos

def random_nsu():
    return "N" + "".join(random.choices(string.ascii_uppercase + string.digits, k=6))

def random_date():
    d = BASE_DATE + timedelta(days=random.randint(0, DATE_RANGE))
    return d.strftime("%d/%m/%Y")

def main():
    with open(OUTPUT, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["DESCRICAO", "CONTA BANCARIA", "DATA COMPETENCIA", "DATA VENCIMENTO", "DATA LANCAMENTO", "VALOR TOTAL", "NSU", "TIPO"])
        for i in range(ROWS):
            comp = BASE_DATE + timedelta(days=random.randint(0, DATE_RANGE))
            venc = comp + timedelta(days=random.randint(1, 30))
            lanc = comp + timedelta(days=random.randint(-5, 15))
            valor = round(random.uniform(10, 500000), 4)
            w.writerow([
                random.choice(DESCRICOES),
                random.choice(CONTAS),
                comp.strftime("%d/%m/%Y"),
                venc.strftime("%d/%m/%Y"),
                lanc.strftime("%d/%m/%Y"),
                valor,
                random_nsu(),
                random.choice(TIPOS),
            ])
            if (i + 1) % 100000 == 0:
                print(f"{i + 1:,} linhas geradas...", file=sys.stderr)
    print(f"Concluído: {ROWS:,} linhas em {OUTPUT}", file=sys.stderr)

if __name__ == "__main__":
    main()
