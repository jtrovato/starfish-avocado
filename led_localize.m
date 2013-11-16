%% Scipt to localize the cassett using two LEDs
close all; clear all;
import vision.*;
%load image
color_img = imread('cassette_im.jpg');
figure(); imshow(color_img);
%threshold image
gray_img = rgb2gray(color_img);
bin_img = im2bw(gray_img, 0.6);
figure(); imshow(bin_img);

%find blobs
CC = bwconncomp(bin_img);
%find centroids of blobs
centroids = regionprops(CC, 'Centroid');

%draw casset square and channels
x1 = round(centroids(1).Centroid(1));
x2 = round(centroids(2).Centroid(1));
y1 = round(centroids(1).Centroid(2));
y2 = round(centroids(2).Centroid(2));

ylen = abs(y2-y1);
xlen = ylen*(35/45);


figure(); imshow(color_img);
hold on;
plot(x2, y2, 'o');
plot(x1, y1, 'o');
plot(x1-xlen, y1, 'o');
plot(x2-xlen, y2, 'o');
plot([x1,x2],[y1 y2],'r', 'LineWidth', 2);
plot([x1-xlen, x2-xlen], [y1,y2], 'r', 'LineWidth', 2);
plot([x1-xlen, x2], [y1-ylen,y2], 'r', 'LineWidth', 2);
plot([x1-xlen, x2], [y1,y1], 'r', 'LineWidth', 2);

figure(); imshow(rgb2gray(imread('control.jpg')));
hold on;
plot(x2, y2, 'o');
plot(x1, y1, 'o');
plot(x1-xlen, y1, 'o');
plot(x2-xlen, y2, 'o');
plot([x1,x2],[y1 y2],'r', 'LineWidth', 2);
plot([x1-xlen, x2-xlen], [y1,y2], 'r', 'LineWidth', 2);
plot([x1-xlen, x2], [y1-ylen,y2], 'r', 'LineWidth', 2);
plot([x1-xlen, x2], [y1,y1], 'r', 'LineWidth', 2);


% gray_img(x1:x1-xlen,y1) = 255;
% gray_img(x2:x2-xlen:y2) =255 ;
% gray_img(x1,y1:y1-ylen) = 255;
% gray_img(x2, y2,y2-ylen) = 255;

%imshow(gray_img);

% yellow = uint8([255 255 0]);
% shapeInserter = vision.ShapeInserter('Shape','Polygons','BorderColor','Custom', 'CustomBorderColor', yellow);
% cas_outline = int32([x1 x2 y1 y2 xlen ylen]);
% J = step(shapeInserter, color_img, cas_outline);
% figure(); imshow(J);

