import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

//vir: https://www.youtube.com/watch?v=gp9H0WLxKgU&ab_channel=ProgrammingandMathTutorials
public class ImageGUI implements ActionListener {
    private BufferedImage image;
    private JFrame frame;
    private JPanel centerTextPanel;
    private JComboBox kernels;


    //vir: https://www.youtube.com/watch?v=PD6pd6AMoOI&list=PLZPZq0r_RZOMhCAyywfnYLlrjiVOkdAI1&index=53&ab_channel=BroCode za border layout
    public ImageGUI(){
        frame = new JFrame("Image GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLayout(new BorderLayout());
        frame.setResizable(true);

        centerTextPanel = new JPanel();
        centerTextPanel.setBackground(Color.WHITE);

        JLabel textlabel = new JLabel("To select an image, click the select button");
        textlabel.setForeground(Color.BLUE);
        textlabel.setFont(new Font("Helvetica", Font.BOLD, 18));
        centerTextPanel.add(textlabel);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.BLUE);

        JButton selectButton = new JButton("Select Image");
        bottomPanel.add(selectButton);
        selectButton.addActionListener(this);

        String[] kernelOptions = {"Edge Detection", "Sharpen", "Blur", "Gaussian blur 3x3"};
        kernels = new JComboBox<String>(kernelOptions);
        bottomPanel.add(kernels);

        JButton processButton = new JButton("Process");
        bottomPanel.add(processButton);

        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesImage();
            }
        });

        centerTextPanel.setPreferredSize(new Dimension(700, 100));
        bottomPanel.setPreferredSize(new Dimension(700, 50));

        frame.add(centerTextPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }



    public void loadImage(String path) {
        try {
            image = ImageIO.read(new File(path));

            if(image == null){
                System.out.println("No image");
                return;
            }

            Image scaledImage = scaleImage(image, frame.getWidth(), frame.getHeight()-100); //bottom panel omejitev


            centerTextPanel.removeAll();
            JLabel label = new JLabel(new ImageIcon(scaledImage));
            centerTextPanel.add(label);
            //vir: https://javarevisited.blogspot.com/2017/04/difference-between-repaint-and-revalidate-in-Swing-Java.html
            centerTextPanel.revalidate(); //layout manager recalculates the layout, ko spremenimo component
            centerTextPanel.repaint(); //redrawing komponente, po spremembi

            frame.setVisible(true);
        } catch (Exception e) {
            System.out.println("Error while loading the image: " + e.getMessage());
        }
    }


    private void procesImage(){

        if (image == null) {
            JOptionPane.showMessageDialog(frame, "You need to load an image", "Error",JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedKernel = (String) kernels.getSelectedItem(); //vrne object, castamo
        if (selectedKernel == null){ //to satisfy IDE :) nemore bit null
            selectedKernel = "Edge Detection";
        }
        double[][] kernel;

        switch (selectedKernel){
            case "Edge Detection":
                kernel = Kernel.EDGE_DETECTION;
                break;
            case "Sharpen":
                kernel = Kernel.SHARPEN;
                break;
            case "Blur":
                kernel = Kernel.BLUR;
                break;
            case "Gaussian blur 3x3":
                kernel = Kernel.GAUSSIAN_BLUR_3;
                break;
            default:
                kernel = Kernel.EDGE_DETECTION;

        }

        BufferedImage finalImage = ImgProcessor.convolution(image, kernel);

        Image scaledImage = scaleImage(finalImage, frame.getWidth(), frame.getHeight()-100); //bottom panel omejitev

        centerTextPanel.removeAll();
        JLabel label = new JLabel(new ImageIcon(scaledImage));
        centerTextPanel.add(label);
        centerTextPanel.revalidate(); //layout manager recalculates the layout, ko spremenimo component
        centerTextPanel.repaint(); //redrawing komponente, po spremembi
    }





    //vir: https://cloudinary.com/guides/bulk-image-resize/3-ways-to-resize-images-in-java#buffered-image
    private Image scaleImage(BufferedImage image, int maxFrameWidth, int maxFrameHeight){
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        double widthScaleRatio = (double) maxFrameWidth / imgWidth;
        double heightScaleRatio = (double) maxFrameHeight / imgHeight;

        double scale = Math.min(widthScaleRatio, heightScaleRatio);

        int newScaledWidth = (int) (imgWidth * scale); //cast ker pixel je whole numbers
        int newScaledHeight = (int) (imgHeight * scale);

        return image.getScaledInstance(newScaledWidth, newScaledHeight, Image.SCALE_SMOOTH);
    }



    //vir: https://docs.oracle.com/javase/8/docs/api/javax/swing/JFileChooser.html za JPG in PNG filter
    //vir: https://www.youtube.com/watch?v=A6sA9KItwpY&list=PLZPZq0r_RZOMhCAyywfnYLlrjiVOkdAI1&index=66&ab_channel=BroCode za response handling
    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select an image");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG & PNG Images", "jpg", "png");
        chooser.setFileFilter(filter);

        int response = chooser.showOpenDialog(frame); //vrne integer 0, če file obstaja, obstaja tudi saveDialog

        if (response == JFileChooser.APPROVE_OPTION){ //uspesno izbran file
            File selectedFile = chooser.getSelectedFile();
            System.out.println("Choosen file: " + selectedFile.getName());
            loadImage(selectedFile.getAbsolutePath());

        }
    }
}

