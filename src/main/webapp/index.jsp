<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- Hero Section -->
<section class="hero-section">
    <div class="container hero-content">
        <div class="row align-items-center">
            <div class="col-lg-6">
                <div class="text-center text-lg-start">
                    <h1 class="display-4 fw-bold mb-3">Welcome to NK Bookstore</h1>
                    <p class="lead mb-4">Discover your next favorite book from our vast collection</p>
                    <div class="search-bar">
                        <div class="input-group">
                            <input type="text" class="form-control" placeholder="Search for books, authors, genres..." aria-label="Search">
                            <button class="btn" type="button"><i class="fas fa-search"></i></button>
                        </div>
                    </div>
                    <div class="mt-4">
                        <a href="#featured" class="btn btn-light btn-lg me-3">Browse Books</a>
                        <a href="register.jsp" class="btn btn-outline-light btn-lg">Join Now</a>
                    </div>
                </div>
            </div>
            <div class="col-lg-6">
                <div class="text-center">
                    <div class="book-image" style="height: 400px; font-size: 8rem;">📚</div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Featured Books Carousel -->
<section id="featured" class="section-bg-light">
    <div class="container">
        <h2 class="section-title">Featured Books</h2>
        <div id="featuredCarousel" class="carousel slide" data-bs-ride="carousel">
            <div class="carousel-inner">
                <div class="carousel-item active">
                    <div class="row">
                        <div class="col-md-4">
                            <div class="book-card">
                                <div class="book-image">📖</div>
                                <div class="book-info">
                                    <h5 class="book-title">The Great Gatsby</h5>
                                    <p class="book-author">F. Scott Fitzgerald</p>
                                    <p class="book-price">$12.99</p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="book-card">
                                <div class="book-image">🕵️</div>
                                <div class="book-info">
                                    <h5 class="book-title">Sherlock Holmes</h5>
                                    <p class="book-author">Arthur Conan Doyle</p>
                                    <p class="book-price">$15.99</p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="book-card">
                                <div class="book-image">🌟</div>
                                <div class="book-info">
                                    <h5 class="book-title">Harry Potter</h5>
                                    <p class="book-author">J.K. Rowling</p>
                                    <p class="book-price">$18.99</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="carousel-item">
                    <div class="row">
                        <div class="col-md-4">
                            <div class="book-card">
                                <div class="book-image">🏰</div>
                                <div class="book-info">
                                    <h5 class="book-title">Pride and Prejudice</h5>
                                    <p class="book-author">Jane Austen</p>
                                    <p class="book-price">$11.99</p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="book-card">
                                <div class="book-image">🚀</div>
                                <div class="book-info">
                                    <h5 class="book-title">Dune</h5>
                                    <p class="book-author">Frank Herbert</p>
                                    <p class="book-price">$16.99</p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="book-card">
                                <div class="book-image">🧙</div>
                                <div class="book-info">
                                    <h5 class="book-title">The Hobbit</h5>
                                    <p class="book-author">J.R.R. Tolkien</p>
                                    <p class="book-price">$14.99</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <button class="carousel-control-prev" type="button" data-bs-target="#featuredCarousel" data-bs-slide="prev">
                <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                <span class="visually-hidden">Previous</span>
            </button>
            <button class="carousel-control-next" type="button" data-bs-target="#featuredCarousel" data-bs-slide="next">
                <span class="carousel-control-next-icon" aria-hidden="true"></span>
                <span class="visually-hidden">Next</span>
            </button>
        </div>
    </div>
</section>

<!-- New Arrivals -->
<section class="py-5">
    <div class="container">
        <h2 class="section-title">New Arrivals</h2>
        <div class="row g-4">
            <div class="col-lg-3 col-md-6">
                <div class="book-card">
                    <div class="book-image">📕</div>
                    <div class="book-info">
                        <h5 class="book-title">Atomic Habits</h5>
                        <p class="book-author">James Clear</p>
                        <p class="book-price">$19.99</p>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <div class="book-card">
                    <div class="book-image">📗</div>
                    <div class="book-info">
                        <h5 class="book-title">The Midnight Library</h5>
                        <p class="book-author">Matt Haig</p>
                        <p class="book-price">$17.99</p>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <div class="book-card">
                    <div class="book-image">📘</div>
                    <div class="book-info">
                        <h5 class="book-title">Educated</h5>
                        <p class="book-author">Tara Westover</p>
                        <p class="book-price">$16.99</p>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <div class="book-card">
                    <div class="book-image">📙</div>
                    <div class="book-info">
                        <h5 class="book-title">The Four Winds</h5>
                        <p class="book-author">Kristin Hannah</p>
                        <p class="book-price">$18.99</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Good Deals -->
<section class="section-bg-light">
    <div class="container">
        <h2 class="section-title">Good Deals</h2>
        <div class="row g-4">
            <div class="col-lg-3 col-md-6">
                <div class="book-card">
                    <div class="book-image">💰</div>
                    <div class="book-info">
                        <h5 class="book-title">To Kill a Mockingbird</h5>
                        <p class="book-author">Harper Lee</p>
                        <p class="book-price"><span class="original">$14.99</span> $9.99</p>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <div class="book-card">
                    <div class="book-image">💸</div>
                    <div class="book-info">
                        <h5 class="book-title">1984</h5>
                        <p class="book-author">George Orwell</p>
                        <p class="book-price"><span class="original">$13.99</span> $8.99</p>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <div class="book-card">
                    <div class="book-image">🤑</div>
                    <div class="book-info">
                        <h5 class="book-title">The Catcher in the Rye</h5>
                        <p class="book-author">J.D. Salinger</p>
                        <p class="book-price"><span class="original">$12.99</span> $7.99</p>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <div class="book-card">
                    <div class="book-image">💵</div>
                    <div class="book-info">
                        <h5 class="book-title">Lord of the Flies</h5>
                        <p class="book-author">William Golding</p>
                        <p class="book-price"><span class="original">$11.99</span> $6.99</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Flash Sale -->
<section class="flash-sale">
    <div class="container">
        <h2 class="fw-bold mb-3">Flash Sale!</h2>
        <p class="lead mb-4">Limited time offer - Up to 50% off on selected books</p>
        <div class="flash-timer" id="countdown">00:00:00</div>
        <div class="mt-4">
            <span class="discount-badge">50% OFF</span>
        </div>
        <a href="#" class="btn btn-light btn-lg mt-4">Shop Now</a>
    </div>
</section>

<!-- Categories -->
<section class="py-5">
    <div class="container">
        <h2 class="section-title">Browse by Category</h2>
        <div class="row g-4">
            <div class="col-lg-3 col-md-6">
                <div class="category-card">
                    <div class="category-icon">📚</div>
                    <h5>Fiction</h5>
                    <p>Explore imaginative worlds</p>
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <div class="category-card">
                    <div class="category-icon">🔬</div>
                    <h5>Non-Fiction</h5>
                    <p>Learn and discover</p>
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <div class="category-card">
                    <div class="category-icon">👶</div>
                    <h5>Children's Books</h5>
                    <p>For young readers</p>
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <div class="category-card">
                    <div class="category-icon">🎓</div>
                    <h5>Educational</h5>
                    <p>Academic and reference</p>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Newsletter -->
<section class="newsletter">
    <div class="container">
        <h2 class="fw-bold mb-3">Stay Updated</h2>
        <p class="lead mb-4">Subscribe to our newsletter for the latest book releases and exclusive deals</p>
        <div class="input-group">
            <input type="email" class="form-control" placeholder="Enter your email" aria-label="Email">
            <button class="btn btn-primary" type="button">Subscribe</button>
        </div>
    </div>
</section>

<script>
// Flash Sale Countdown
function updateCountdown() {
    const now = new Date().getTime();
    const endTime = now + (24 * 60 * 60 * 1000); // 24 hours from now
    const distance = endTime - now;

    const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((distance % (1000 * 60)) / 1000);

    document.getElementById("countdown").innerHTML = hours.toString().padStart(2, '0') + ":" + minutes.toString().padStart(2, '0') + ":" + seconds.toString().padStart(2, '0');

    if (distance < 0) {
        clearInterval(x);
        document.getElementById("countdown").innerHTML = "EXPIRED";
    }
}

updateCountdown();
const x = setInterval(updateCountdown, 1000);
</script>
