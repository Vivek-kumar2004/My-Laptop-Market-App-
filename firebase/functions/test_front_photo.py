import unittest

from catalog import _display_images


class FrontPhotoTest(unittest.TestCase):
    def test_front_photo_leads_and_gallery_follows(self):
        front, names, urls = _display_images(
            {"front_image": "front.jpg", "images": ["side.jpg", "back.jpg"]}
        )
        self.assertEqual("front.jpg", front.split("/")[-1])
        self.assertEqual(["side.jpg", "back.jpg"], names)
        self.assertEqual(
            ["front.jpg", "side.jpg", "back.jpg"],
            [url.split("/")[-1] for url in urls],
        )

    def test_old_listing_keeps_its_first_photo(self):
        front, _, urls = _display_images({"images": ["a.jpg", "b.jpg"]})
        self.assertEqual("", front)
        self.assertEqual(["a.jpg", "b.jpg"], [url.split("/")[-1] for url in urls])


if __name__ == "__main__":
    unittest.main()
