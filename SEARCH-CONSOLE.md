# Index the RealCTS website in Google Search

The repository publishes the site from `main` and `/docs`. Its canonical homepage is [the RealCTS project site](https://itzsnsr.github.io/RealCTS/).

1. Sign in to [Google Search Console](https://search.google.com/search-console/) with the Google account that will manage this site.
2. Choose **Add property → URL prefix**. Enter `https://itzsnsr.github.io/RealCTS/`, including the capital letters and trailing slash.
3. Select **HTML tag** under ownership verification. Copy the exact meta tag from Google into the `<head>` of `docs/index.html`, commit it to `main`, and wait for Pages to deploy. View the published homepage source to confirm the tag is present, then select **Verify** in Search Console. Leave the tag in place to retain verification. [Google’s ownership verification guide](https://support.google.com/webmasters/answer/9008080)
4. Open **Sitemaps** for the verified property and submit `sitemap.xml` under that URL prefix. Confirm the submitted address is [the project sitemap](https://itzsnsr.github.io/RealCTS/sitemap.xml).
5. In **URL inspection**, enter the full homepage URL. Use **Test live URL** to check access, then **Request indexing**. [Google’s recrawl guide](https://developers.google.com/search/docs/crawling-indexing/ask-google-to-recrawl)
6. Monitor **Sitemaps** for processing errors and **Page indexing** for indexing status. Use **URL inspection** to see the last crawl and Google-selected canonical. Once impressions appear, **Performance** shows which searches lead to the site. Submission does not guarantee indexing or ranking.

GitHub serves this project under the `/RealCTS/` path. Crawlers look for robots rules at the host root. The project's `docs/robots.txt` is published at `/RealCTS/robots.txt`; submit the sitemap directly for this property. [Google’s robots.txt location rules](https://developers.google.com/search/docs/crawling-indexing/robots/robots_txt)
