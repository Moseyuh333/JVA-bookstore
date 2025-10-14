<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Book Details | Bookish Bliss Haven</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500&display=swap');
    body { font-family: 'Roboto', sans-serif; background-color: #111; color: #eee; }
    .title-font { font-family: 'Playfair Display', serif; }
  </style>
</head>
<body class="min-h-screen flex flex-col">
  <!-- Header -->
  <nav class="bg-amber-800 text-white shadow-lg">
    <div class="container mx-auto px-4 py-4 flex justify-between items-center">
      <a href="${pageContext.request.contextPath}/index.jsp" class="flex items-center gap-2 text-xl font-bold title-font">
        <i data-feather="book-open"></i> Bookish Bliss Haven
      </a>
      <a href="${pageContext.request.contextPath}/index.jsp" class="text-amber-200 hover:text-white">← Back to Home</a>
    </div>
  </nav>

  <!-- Book detail section -->
  <main class="flex-grow">
    <div class="container mx-auto px-6 py-12">
      <div class="bg-neutral-900 rounded-xl shadow-lg overflow-hidden grid grid-cols-1 md:grid-cols-2">
        <!-- Book Image -->
        <div class="flex items-center justify-center bg-neutral-800 p-8">
          <img src="${book.coverImage}" alt="${book.title}"
               class="max-h-[400px] rounded shadow-md object-contain">
        </div>

        <!-- Book Info -->
        <div class="p-8 md:p-12">
          <h1 class="title-font text-3xl md:text-4xl font-bold text-amber-400 mb-3">${book.title}</h1>
          <p class="italic text-gray-400 mb-2">by ${book.author != null ? book.author : "Unknown author"}</p>
          <p class="text-amber-500 mb-1"><strong>Category:</strong> ${book.category}</p>

          <p class="${book.stock_text eq 'In stock' ? 'text-green-400' : 'text-red-400'} font-semibold mb-2">
            ${book.stock_text}
          </p>

          <p class="text-2xl font-bold text-amber-400 mb-4">$${book.price}</p>

          <p class="text-gray-300 leading-relaxed mb-6">${book.description}</p>

          <button class="bg-amber-600 hover:bg-amber-700 text-white font-semibold px-6 py-3 rounded-full transition">
            Add to Cart
          </button>
        </div>
      </div>
    </div>
  </main>

  <!-- Footer -->
  <footer class="bg-neutral-950 text-gray-400 text-center py-6 mt-12 text-sm">
    © <span id="year"></span> Bookish Bliss Haven — All rights reserved.
  </footer>

  <script src="https://unpkg.com/feather-icons"></script>
  <script>
    feather.replace();
    document.getElementById('year').textContent = new Date().getFullYear();
  </script>
</body>
</html>
