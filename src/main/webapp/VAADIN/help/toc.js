// Populate the sidebar
//
// This is a script, and not included directly in the page, to control the total size of the book.
// The TOC contains an entry for each page, so if each page includes a copy of the TOC,
// the total size of the page becomes O(n**2).
class MDBookSidebarScrollbox extends HTMLElement {
    constructor() {
        super();
    }
    connectedCallback() {
        this.innerHTML = '<ol class="chapter"><li class="chapter-item expanded "><a href="index.html"><strong aria-hidden="true">1.</strong> Introduction</a></li><li class="chapter-item expanded "><a href="interface/index.html"><strong aria-hidden="true">2.</strong> Using the ANNIS interface</a></li><li><ol class="section"><li class="chapter-item expanded "><a href="interface/search-form.html"><strong aria-hidden="true">2.1.</strong> Search Form</a></li><li class="chapter-item expanded "><a href="interface/result-window.html"><strong aria-hidden="true">2.2.</strong> Result Window</a></li><li class="chapter-item expanded "><a href="interface/query-builder.html"><strong aria-hidden="true">2.3.</strong> Query Builder</a></li></ol></li><li class="chapter-item expanded "><a href="aql/index.html"><strong aria-hidden="true">3.</strong> ANNIS Query Language (AQL)</a></li><li><ol class="section"><li class="chapter-item expanded "><a href="aql/word-forms.html"><strong aria-hidden="true">3.1.</strong> Searching for Word Forms</a></li><li class="chapter-item expanded "><a href="aql/annotations.html"><strong aria-hidden="true">3.2.</strong> Searching for Annotations</a></li><li class="chapter-item expanded "><a href="aql/regex.html"><strong aria-hidden="true">3.3.</strong> Searching using Regular Expressions</a></li><li class="chapter-item expanded "><a href="aql/trees.html"><strong aria-hidden="true">3.4.</strong> Searching for Trees</a></li><li class="chapter-item expanded "><a href="aql/pointing.html"><strong aria-hidden="true">3.5.</strong> Searching for Pointing Relations</a></li><li class="chapter-item expanded "><a href="aql/negation.html"><strong aria-hidden="true">3.6.</strong> Operator Negation</a></li><li class="chapter-item expanded "><a href="aql/export.html"><strong aria-hidden="true">3.7.</strong> Exporting Results</a></li><li class="chapter-item expanded "><a href="aql/frequency.html"><strong aria-hidden="true">3.8.</strong> Frequency Analysis</a></li><li class="chapter-item expanded "><a href="aql/operators.html"><strong aria-hidden="true">3.9.</strong> Complete List of Operators</a></li><li class="chapter-item expanded "><a href="aql/compatibility-mode.html"><strong aria-hidden="true">3.10.</strong> Differences in Compatibility Mode</a></li></ol></li></ol>';
        // Set the current, active page, and reveal it if it's hidden
        let current_page = document.location.href.toString().split("#")[0].split("?")[0];
        if (current_page.endsWith("/")) {
            current_page += "index.html";
        }
        var links = Array.prototype.slice.call(this.querySelectorAll("a"));
        var l = links.length;
        for (var i = 0; i < l; ++i) {
            var link = links[i];
            var href = link.getAttribute("href");
            if (href && !href.startsWith("#") && !/^(?:[a-z+]+:)?\/\//.test(href)) {
                link.href = path_to_root + href;
            }
            // The "index" page is supposed to alias the first chapter in the book.
            if (link.href === current_page || (i === 0 && path_to_root === "" && current_page.endsWith("/index.html"))) {
                link.classList.add("active");
                var parent = link.parentElement;
                if (parent && parent.classList.contains("chapter-item")) {
                    parent.classList.add("expanded");
                }
                while (parent) {
                    if (parent.tagName === "LI" && parent.previousElementSibling) {
                        if (parent.previousElementSibling.classList.contains("chapter-item")) {
                            parent.previousElementSibling.classList.add("expanded");
                        }
                    }
                    parent = parent.parentElement;
                }
            }
        }
        // Track and set sidebar scroll position
        this.addEventListener('click', function(e) {
            if (e.target.tagName === 'A') {
                sessionStorage.setItem('sidebar-scroll', this.scrollTop);
            }
        }, { passive: true });
        var sidebarScrollTop = sessionStorage.getItem('sidebar-scroll');
        sessionStorage.removeItem('sidebar-scroll');
        if (sidebarScrollTop) {
            // preserve sidebar scroll position when navigating via links within sidebar
            this.scrollTop = sidebarScrollTop;
        } else {
            // scroll sidebar to current active section when navigating via "next/previous chapter" buttons
            var activeSection = document.querySelector('#sidebar .active');
            if (activeSection) {
                activeSection.scrollIntoView({ block: 'center' });
            }
        }
        // Toggle buttons
        var sidebarAnchorToggles = document.querySelectorAll('#sidebar a.toggle');
        function toggleSection(ev) {
            ev.currentTarget.parentElement.classList.toggle('expanded');
        }
        Array.from(sidebarAnchorToggles).forEach(function (el) {
            el.addEventListener('click', toggleSection);
        });
    }
}
window.customElements.define("mdbook-sidebar-scrollbox", MDBookSidebarScrollbox);
