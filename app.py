from flask import Flask, render_template, redirect, url_for, request

app = Flask(__name__)

products = [
    {
        "id": 1,
        "name": "Laptop",
        "price": "1200",
        "description": "Gaming Laptop",
        "image": "laptop1.jpg",
    },

    {
        "id": 3,
        "name": "Tablet",
        "price": "500",
        "description": "Test",
        "image": "tab1.jpg",
    },
    {
        "id": 5,
        "name": "WebTest",
        "price": "99",
        "description": "FromWeb",
        "image": "des1.jpg",
    },
    {
        "id": 7,
        "name": "ja",
        "price": "6500.00",
        "description": "kjnuhyg",
        "image": "yy1.jpg",
    },
]













# -----------------------------
# عرض المنتجات
# -----------------------------
@app.route("/")
def index():

    return render_template("index.html", products=products)

# -----------------------------
# إضافة منتج
# -----------------------------
@app.route("/add", methods=["GET", "POST"])
def add_product():
    if request.method == "POST":
        new_id = products[-1]["id"] + 1 if products else 1

        products.append({
            "id": new_id,
            "name": request.form.get("name"),
            "price": request.form.get("price"),
            "description": request.form.get("description"),
            "image": request.form.get("image", "default.jpg")

        })

        return redirect(url_for("index"))

    return render_template("add_product.html")


# -----------------------------
# تفاصيل منتج
# ----------------------------

@app.route("/product/<int:product_id>")
def details(product_id):
    for product in products:
        if product["id"] == product_id:
            return render_template("details.html", product=product)
    return "Not Found", 404


# -----------------------------
# حذف منتج
# -----------------------------
@app.route('/delete/<int:product_id>', methods=['POST'])
def delete_product_route(product_id):
    global products
    new_products = []
    for p in products:
        if p["id"] != product_id:
            new_products.append(p)
    products = new_products
    return redirect(url_for("index"))


# -----------------------------
# تشغيل التطبيق
# -----------------------------
if __name__ == "__main__":
    app.run(debug=True)
