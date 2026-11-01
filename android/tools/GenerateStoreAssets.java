import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Code-native brand artwork and lossless screenshot padding to Google Play's 9:16 phone ratio. */
public class GenerateStoreAssets {
    static final Color AMBER = new Color(245,166,35), DARK = new Color(24,18,10);
    static Graphics2D graphics(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0,0,DARK,0,image.getHeight(),new Color(43,28,8)));
        g.fillRect(0,0,image.getWidth(),image.getHeight()); return g;
    }
    static void mug(Graphics2D g, int x, int y, double scale) {
        AffineTransform before = g.getTransform(); g.translate(x,y); g.scale(scale,scale);
        g.setColor(AMBER); g.setStroke(new BasicStroke(13,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g.draw(new RoundRectangle2D.Double(117,42,52,90,28,28));
        g.fill(new RoundRectangle2D.Double(12,5,120,175,44,44));
        g.fillRect(12,25,120,90);
        g.setColor(DARK); g.fill(new Ellipse2D.Double(26,10,92,22));
        g.setTransform(before);
    }
    public static void main(String[] args) throws Exception {
        File output = new File(args[0]); output.mkdirs();
        BufferedImage icon = new BufferedImage(512,512,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = graphics(icon); mug(g,111,98,1.85); g.dispose();
        ImageIO.write(icon,"png",new File(output,"icon-512.png"));
        BufferedImage feature = new BufferedImage(1024,500,BufferedImage.TYPE_INT_RGB);
        g = graphics(feature);
        g.setColor(new Color(245,166,35,15)); g.fillOval(678,-70,530,530);
        g.setFont(new Font("Segoe UI",Font.BOLD,76)); g.setColor(AMBER); g.drawString("SayBeer",64,150);
        g.setFont(new Font("Segoe UI",Font.BOLD,38)); g.setColor(Color.WHITE); g.drawString("Giữ lời với chính mình.",68,224);
        g.setFont(new Font("Segoe UI",Font.PLAIN,24)); g.setColor(new Color(201,187,166));
        g.drawString("Chọn app. Hẹn giờ. Tự mở đúng hẹn.",68,286);
        g.setFont(new Font("Segoe UI",Font.PLAIN,21)); g.drawString("Không tài khoản · Không quảng cáo",68,404);
        mug(g,736,144,1.5); g.dispose();
        ImageIO.write(feature,"png",new File(output,"feature-1024x500.png"));
        if(args.length > 1) {
            File screenshots = new File(args[1]);
            File destination = new File(output,"phone"); destination.mkdirs();
            File[] files = screenshots.listFiles((d,n)->n.endsWith(".png"));
            if(files != null) for(File file: files) {
                BufferedImage original = ImageIO.read(file);
                int h = ((Math.max(original.getHeight(), (original.getWidth()*16+8)/9) + 15)/16)*16;
                int w = h*9/16;
                BufferedImage frame = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
                Graphics2D canvas = graphics(frame); canvas.drawImage(original,(w-original.getWidth())/2,(h-original.getHeight())/2,null); canvas.dispose();
                ImageIO.write(frame,"png",new File(destination,file.getName()));
            }
        }
        System.out.println("Store artwork exported to " + output.getCanonicalPath());
    }
}
