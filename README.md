# photoGallery
Simple RESTlike Photo Gallery service, which offers to user to create a photo gallery by uploading photos.

Starting page of service is <code>localhost:8080/photo</code>.

User can upload only <code>*.png</code> files.

<h3>Additional RESTlike options</h3>

<code>/photo/original</code> - shows original sizes of uploaded images;

<code>/photo/row/{number}</code> - sets up a number of rows of images on the page;

<code>/photo/wh/{XXXxZZZ}</code> - sets a size of image on the html page. XXX - widht, ZZZ - height;

<code>/photo/blackbackground</code> - sets a black background to the gallery page;
