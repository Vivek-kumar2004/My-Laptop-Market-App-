from __future__ import annotations

from io import BytesIO
from urllib.request import urlopen

from PIL import Image, ImageOps

LOGO_URL = "https://storage.googleapis.com/mylaptopmarket-app-web/images/mylaptoplogo.png"
_LOGO = None


def _logo():
    global _LOGO
    if _LOGO is False:
        return None
    if _LOGO is not None:
        return _LOGO.copy()
    try:
        with urlopen(LOGO_URL, timeout=15) as response:
            _LOGO = Image.open(BytesIO(response.read())).convert("RGBA")
    except Exception:
        _LOGO = False
        return None
    return _LOGO.copy()


def _crop_empty(src: Image.Image) -> Image.Image:
    pixels = src.load()
    width, height = src.size
    min_x, min_y, max_x, max_y = width, height, 0, 0
    for y in range(height):
        for x in range(width):
            red, green, blue, alpha = pixels[x, y]
            if alpha < 4 or (red > 245 and green > 245 and blue > 245):
                continue
            min_x = min(min_x, x)
            min_y = min(min_y, y)
            max_x = max(max_x, x)
            max_y = max(max_y, y)
    if max_x <= min_x or max_y <= min_y:
        return src
    pad = 8
    box = (
        max(0, min_x - pad),
        max(0, min_y - pad),
        min(width, max_x + pad + 1),
        min(height, max_y + pad + 1),
    )
    if box[2] - box[0] >= width - 4 and box[3] - box[1] >= height - 4:
        return src
    return src.crop(box)


def _clear_white(src: Image.Image) -> Image.Image:
    out = Image.new("RGBA", src.size)
    src_pixels = src.load()
    out_pixels = out.load()
    width, height = src.size
    for y in range(height):
        for x in range(width):
            red, green, blue, alpha = src_pixels[x, y]
            if red > 245 and green > 245 and blue > 245:
                out_pixels[x, y] = (0, 0, 0, 0)
            else:
                out_pixels[x, y] = (red, green, blue, alpha)
    return out


def _stamp(base: Image.Image, logo: Image.Image) -> Image.Image:
    logo = _clear_white(_crop_empty(logo))
    target_w = max(90, min(220, int(base.width * 0.18)))
    target_h = max(70, int(round(logo.height * (target_w / max(1, logo.width)))))
    stamp = logo.resize((target_w, target_h), Image.Resampling.LANCZOS)
    pad = max(10, int(base.width * 0.025))
    origin_x = base.width - stamp.width - pad
    origin_y = base.height - stamp.height - pad
    pixels = base.load()
    stamp_pixels = stamp.load()
    opacity = 0.62
    for y in range(stamp.height):
        for x in range(stamp.width):
            red, green, blue, alpha = stamp_pixels[x, y]
            if alpha < 4:
                continue
            dest_x = origin_x + x
            dest_y = origin_y + y
            if dest_x < 0 or dest_y < 0 or dest_x >= base.width or dest_y >= base.height:
                continue
            dest_red, dest_green, dest_blue, dest_alpha = pixels[dest_x, dest_y]
            cover = (alpha / 255) * opacity
            pixels[dest_x, dest_y] = (
                int(red * cover + dest_red * (1 - cover)),
                int(green * cover + dest_green * (1 - cover)),
                int(blue * cover + dest_blue * (1 - cover)),
                dest_alpha,
            )
    return base


def apply_watermark(data: bytes, ext: str) -> tuple[bytes, str | None]:
    try:
        image = ImageOps.exif_transpose(Image.open(BytesIO(data)))
        if image.width < 80 or image.height < 80:
            return data, None
        logo = _logo()
        if logo is None:
            return data, None
        marked = _stamp(image.convert("RGBA"), logo)
        output = BytesIO()
        kind = ext.lower()
        if kind == "png":
            marked.save(output, format="PNG")
            content_type = "image/png"
        elif kind == "gif":
            marked.convert("P").save(output, format="GIF")
            content_type = "image/gif"
        elif kind == "webp":
            marked.convert("RGB").save(output, format="WEBP", quality=88)
            content_type = "image/webp"
        else:
            marked.convert("RGB").save(output, format="JPEG", quality=88)
            content_type = "image/jpeg"
        return output.getvalue(), content_type
    except Exception:
        return data, None
